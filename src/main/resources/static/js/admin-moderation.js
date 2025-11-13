(() => {
    document.addEventListener('DOMContentLoaded', () => {
        const form = document.getElementById('statusFilterForm');
        if (!form) return;

        const statusInput = document.getElementById('statusInput');
        const pageInput = document.getElementById('pageInput');
        const chips = form.querySelectorAll('.mod-chip');

        chips.forEach((chip) => {
            chip.addEventListener('click', () => {
                const statusValue = chip.dataset.status ?? '';
                if (statusInput) {
                    statusInput.value = statusValue;
                }
                if (pageInput) {
                    pageInput.value = 0;
                }
                form.submit();
            });
        });
    });
})();

