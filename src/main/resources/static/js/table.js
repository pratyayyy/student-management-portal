// Search functionality
const searchInput = document.getElementById("searchInput");
searchInput.addEventListener("keyup", function() {
    const filter = searchInput.value.toLowerCase();
    const rows = document.querySelectorAll("#studentTable tbody tr");

    rows.forEach(row => {
        const cells = row.querySelectorAll("td");
        const match = Array.from(cells).some(td => td.textContent.toLowerCase().includes(filter));
        row.style.display = match ? "" : "none";
    });
});

// Sort table function
function sortTable(columnIndex) {
    const table = document.getElementById("studentTable");
    const tbody = table.tBodies[0];
    const rows = Array.from(tbody.querySelectorAll("tr"));
    const ascending = !tbody.getAttribute("data-sort-asc") || tbody.getAttribute("data-sort-col") != columnIndex;

    rows.sort((a, b) => {
        let aText = a.children[columnIndex].textContent.trim();
        let bText = b.children[columnIndex].textContent.trim();

        // Compare numbers or strings
        const aNum = parseFloat(aText);
        const bNum = parseFloat(bText);
        if (!isNaN(aNum) && !isNaN(bNum)) {
            return ascending ? aNum - bNum : bNum - aNum;
        }
        return ascending ? aText.localeCompare(bText) : bText.localeCompare(aText);
    });

    rows.forEach(row => tbody.appendChild(row));

    tbody.setAttribute("data-sort-asc", ascending);
    tbody.setAttribute("data-sort-col", columnIndex);
}
