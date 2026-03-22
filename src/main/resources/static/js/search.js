/**
 * Student Search and Pagination Module
 * Handles real-time search filtering and pagination controls
 */

(function() {
    'use strict';

    // Configuration
    const config = {
        debounceDelay: 300,
        apiSearchEndpoint: '/api/students/search',
        apiPaginatedEndpoint: '/api/students/paginated',
        defaultPageSize: 10,
        pageSizeOptions: [10, 20, 30],
    };

    // State management
    let state = {
        currentPage: 0,
        pageSize: config.defaultPageSize,
        searchQuery: '',
        totalPages: 0,
        isLoading: false,
        debounceTimer: null,
    };

    // DOM elements
    let elements = {};

    /**
     * Initialize the search and pagination functionality
     */
    function init() {
        cacheElements();
        if (elements.searchInput) {
            attachEventListeners();
            loadInitialData();
        }
    }

    /**
     * Cache DOM elements for better performance
     */
    function cacheElements() {
        elements = {
            searchInput: document.getElementById('studentSearchInput'),
            searchContainer: document.getElementById('studentSearchContainer'),
            pageSizeSelect: document.getElementById('pageSizeSelect'),
            studentTable: document.getElementById('studentTable'),
            tableBody: document.querySelector('table tbody'),
            noResultsMessage: document.getElementById('noResultsMessage'),
            loadingSpinner: document.getElementById('loadingSpinner'),
            paginationContainer: document.getElementById('paginationControls'),
            pageInfo: document.getElementById('pageInfo'),
            prevPageBtn: document.getElementById('prevPageBtn'),
            nextPageBtn: document.getElementById('nextPageBtn'),
            paginationButtons: document.getElementById('paginationButtons'),
        };
    }

    /**
     * Attach event listeners to interactive elements
     */
    function attachEventListeners() {
        // Search input with debouncing
        if (elements.searchInput) {
            elements.searchInput.addEventListener('input', function(e) {
                clearTimeout(state.debounceTimer);
                state.debounceTimer = setTimeout(() => {
                    state.searchQuery = e.target.value.trim();
                    state.currentPage = 0; // Reset to first page on new search
                    performSearch();
                }, config.debounceDelay);
            });
        }

        // Page size selector
        if (elements.pageSizeSelect) {
            elements.pageSizeSelect.addEventListener('change', function(e) {
                state.pageSize = parseInt(e.target.value, 10);
                state.currentPage = 0; // Reset to first page when changing page size
                performSearch();
            });
        }

        // Pagination buttons
        if (elements.prevPageBtn) {
            elements.prevPageBtn.addEventListener('click', previousPage);
        }
        if (elements.nextPageBtn) {
            elements.nextPageBtn.addEventListener('click', nextPage);
        }
    }

    /**
     * Load initial data on page load
     */
    function loadInitialData() {
        performSearch();
    }

    /**
     * Perform search or load all students with pagination
     */
    function performSearch() {
        state.isLoading = true;
        showLoadingSpinner();

        const endpoint = state.searchQuery.length > 0
            ? `${config.apiSearchEndpoint}?query=${encodeURIComponent(state.searchQuery)}&page=${state.currentPage}&pageSize=${state.pageSize}`
            : `${config.apiPaginatedEndpoint}?page=${state.currentPage}&pageSize=${state.pageSize}`;

        fetch(endpoint)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                state.isLoading = false;
                hideLoadingSpinner();
                updateStudentTable(data);
                updatePaginationControls(data);
            })
            .catch(error => {
                console.error('Error fetching data:', error);
                state.isLoading = false;
                hideLoadingSpinner();
                showErrorMessage('Error loading students. Please try again.');
            });
    }

    /**
     * Update the student table with new data
     * @param {Object} data - Paginated response from API
     */
    function updateStudentTable(data) {
        if (!elements.tableBody) return;

        // Clear existing rows
        elements.tableBody.innerHTML = '';

        // Check if there are no results
        if (!data.content || data.content.length === 0) {
            showNoResultsMessage();
            elements.tableBody.innerHTML = '';
            return;
        }

        hideNoResultsMessage();

        // Create rows for each student
        data.content.forEach(student => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${escapeHtml(student.studentId || '')}</td>
                <td>${escapeHtml(student.name || '')}</td>
                <td>${escapeHtml(student.phoneNumber || '')}</td>
                <td>${escapeHtml(student.alternateNumber || '')}</td>
                <td>${escapeHtml(student.standard || '')}</td>
                <td>${escapeHtml(student.address || '')}</td>
                <td>${escapeHtml(student.guardiansName || '')}</td>
                <td>
                    <a class="btn" href="/students/${encodeURIComponent(student.studentId)}">
                        View Details
                    </a>
                </td>
            `;
            elements.tableBody.appendChild(row);
        });

        state.totalPages = data.totalPages;
    }

    /**
     * Update pagination controls
     * @param {Object} data - Paginated response from API
     */
    function updatePaginationControls(data) {
        if (!elements.paginationContainer) return;

        // Show pagination controls if there are results
        if (data.content && data.content.length > 0) {
            elements.paginationContainer.style.display = 'flex';
        } else {
            elements.paginationContainer.style.display = 'none';
        }

        // Update page info
        if (elements.pageInfo) {
            const startRecord = (data.currentPage * data.pageSize) + 1;
            const endRecord = Math.min((data.currentPage + 1) * data.pageSize, data.totalElements);
            elements.pageInfo.textContent = `Page ${data.currentPage + 1} of ${data.totalPages} | Showing ${startRecord}-${endRecord} of ${data.totalElements} records`;
        }

        // Update previous button state
        if (elements.prevPageBtn) {
            elements.prevPageBtn.disabled = !data.hasPreviousPage;
        }

        // Update next button state
        if (elements.nextPageBtn) {
            elements.nextPageBtn.disabled = !data.hasNextPage;
        }

        // Update page number buttons
        if (elements.paginationButtons) {
            updatePageNumberButtons(data);
        }
    }

    /**
     * Update page number buttons for quick navigation
     * @param {Object} data - Paginated response from API
     */
    function updatePageNumberButtons(data) {
        elements.paginationButtons.innerHTML = '';

        const totalPages = data.totalPages;
        const currentPage = data.currentPage;
        const maxButtonsToShow = 5;

        // Calculate range of pages to show
        let startPage = Math.max(0, currentPage - Math.floor(maxButtonsToShow / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxButtonsToShow - 1);

        // Adjust start if we're near the end
        if (endPage - startPage < maxButtonsToShow - 1) {
            startPage = Math.max(0, endPage - maxButtonsToShow + 1);
        }

        // Add page buttons
        for (let i = startPage; i <= endPage; i++) {
            const btn = document.createElement('button');
            btn.textContent = (i + 1).toString();
            btn.classList.add('page-btn');
            if (i === currentPage) {
                btn.classList.add('active');
                btn.disabled = true;
            }
            btn.addEventListener('click', () => goToPage(i));
            elements.paginationButtons.appendChild(btn);
        }
    }

    /**
     * Go to specific page
     * @param {number} pageNumber - Zero-indexed page number
     */
    function goToPage(pageNumber) {
        state.currentPage = pageNumber;
        performSearch();
    }

    /**
     * Go to previous page
     */
    function previousPage() {
        if (state.currentPage > 0) {
            state.currentPage--;
            performSearch();
            scrollToTable();
        }
    }

    /**
     * Go to next page
     */
    function nextPage() {
        if (state.currentPage < state.totalPages - 1) {
            state.currentPage++;
            performSearch();
            scrollToTable();
        }
    }

    /**
     * Show loading spinner
     */
    function showLoadingSpinner() {
        if (elements.loadingSpinner) {
            elements.loadingSpinner.style.display = 'flex';
        }
    }

    /**
     * Hide loading spinner
     */
    function hideLoadingSpinner() {
        if (elements.loadingSpinner) {
            elements.loadingSpinner.style.display = 'none';
        }
    }

    /**
     * Show "no results" message
     */
    function showNoResultsMessage() {
        if (elements.noResultsMessage) {
            elements.noResultsMessage.style.display = 'block';
        }
    }

    /**
     * Hide "no results" message
     */
    function hideNoResultsMessage() {
        if (elements.noResultsMessage) {
            elements.noResultsMessage.style.display = 'none';
        }
    }

    /**
     * Show error message
     * @param {string} message - Error message to display
     */
    function showErrorMessage(message) {
        // Create alert or notification
        console.error(message);
        showNoResultsMessage();
    }

    /**
     * Scroll to the table when pagination changes
     */
    function scrollToTable() {
        if (elements.studentTable) {
            elements.studentTable.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    /**
     * Escape HTML to prevent XSS attacks
     * @param {string} text - Text to escape
     * @returns {string} Escaped text
     */
    function escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, m => map[m]);
    }

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Export for testing or external use
    window.studentSearch = {
        goToPage,
        performSearch,
    };
})();
