#!/bin/bash

# Docker Parallel Execution Management Script
# Usage: ./docker-parallel.sh [command]

set -e

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_COMPOSE_FILE="$PROJECT_ROOT/docker/docker-compose.yml"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi
    log_success "Docker and Docker Compose are installed"
}

# Start Docker containers
start_docker() {
    log_info "Starting Selenium Grid containers..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" up -d
    log_info "Waiting for containers to be ready..."
    sleep 15

    # Check if hub is ready
    if docker-compose -f "$DOCKER_COMPOSE_FILE" ps | grep -q "selenium-hub.*Up"; then
        log_success "Selenium Hub is ready"
    else
        log_error "Selenium Hub failed to start"
        docker-compose -f "$DOCKER_COMPOSE_FILE" logs selenium-hub
        exit 1
    fi

    log_success "All containers started successfully"
    show_status
}

# Stop Docker containers
stop_docker() {
    log_info "Stopping Selenium Grid containers..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" down
    log_success "Containers stopped"
}

# Show container status
show_status() {
    log_info "Container Status:"
    docker-compose -f "$DOCKER_COMPOSE_FILE" ps

    log_info "\nNode Details:"
    log_info "Selenium Hub Console: http://localhost:4444"
    log_info "VNC Access Points:"
    log_info "  Chrome Node 1: localhost:7900 (password: secret)"
    log_info "  Chrome Node 2: localhost:7901 (password: secret)"
    log_info "  Chrome Node 3: localhost:7902 (password: secret)"
    log_info "  Firefox Node 1: localhost:7903 (password: secret)"
    log_info "  Firefox Node 2: localhost:7904 (password: secret)"
}

# View logs
show_logs() {
    log_info "Showing Selenium Hub logs..."
    docker-compose -f "$DOCKER_COMPOSE_FILE" logs -f selenium-hub
}

# Run parallel tests
run_tests() {
    local test_suite="${1:-testng-docker-parallel.xml}"

    log_info "Running tests with suite: $test_suite"

    if [ ! -f "$PROJECT_ROOT/$test_suite" ]; then
        log_error "Test suite not found: $test_suite"
        exit 1
    fi

    cd "$PROJECT_ROOT"
    mvn clean test -Dsuites="$test_suite" -Dheadless=true
}

# Run all parallel tests
run_all_parallel() {
    log_info "Running all parallel tests (class-level parallelization)..."
    run_tests "testng-docker-all-parallel.xml"
}

# View grid status
grid_status() {
    log_info "Checking Selenium Grid status..."
    curl -s http://localhost:4444/wd/hub/status | jq . 2>/dev/null || \
        log_error "Could not connect to Selenium Grid. Is it running?"
}

# Clean up and restart
restart() {
    log_warn "Restarting Docker containers..."
    stop_docker
    sleep 3
    start_docker
}

# Show help
show_help() {
    cat << EOF
Docker Parallel Execution Management Script

Usage: $0 [command]

Commands:
    start           Start Selenium Grid containers
    stop            Stop Selenium Grid containers
    status          Show container status
    logs            Show Selenium Hub logs
    test            Run parallel tests (testng-docker-parallel.xml)
    test-all        Run all parallel tests (testng-docker-all-parallel.xml)
    grid-status     Check Selenium Grid status
    restart         Restart all containers
    help            Show this help message

Examples:
    # Start Docker Grid
    ./docker-parallel.sh start

    # Run tests
    ./docker-parallel.sh test

    # Check status
    ./docker-parallel.sh status

    # Stop containers
    ./docker-parallel.sh stop

EOF
}

# Main script logic
main() {
    command="${1:-help}"

    case "$command" in
        start)
            check_docker
            start_docker
            ;;
        stop)
            stop_docker
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        test)
            run_tests "testng-docker-parallel.xml"
            ;;
        test-all)
            run_all_parallel
            ;;
        grid-status)
            grid_status
            ;;
        restart)
            restart
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "Unknown command: $command"
            show_help
            exit 1
            ;;
    esac
}

main "$@"

