(() => {
    function selectAll(selector) {
        return Array.from(document.querySelectorAll(selector));
    }

    function showToast(message, type = 'info') {
        const toast = document.createElement('div');
        toast.className = `mod-toast ${type ? `mod-toast-${type}` : ''}`;
        toast.innerHTML = `
            <i class="fas fa-circle-info"></i>
            <span>${message}</span>
            <button type="button" aria-label="close">
                <i class="fas fa-xmark"></i>
            </button>
        `;

        toast.querySelector('button').addEventListener('click', () => toast.remove());

        document.body.appendChild(toast);
        setTimeout(() => {
            toast.classList.add('fade');
            setTimeout(() => toast.remove(), 250);
        }, 4200);
    }

    function bindFormConfirmations() {
        selectAll('form[data-mod-action]').forEach((form) => {
            form.addEventListener('submit', (event) => {
                const action = form.getAttribute('data-mod-action');
                const confirmMessage = action === 'approve'
                    ? 'Xác nhận phê duyệt và ẩn review?'
                    : 'Xác nhận từ chối báo cáo và giữ nguyên review?';

                if (!confirm(confirmMessage)) {
                    event.preventDefault();
                    return;
                }

                const submitButton = form.querySelector('button[type="submit"]');
                if (submitButton) {
                    submitButton.classList.add('is-loading');
                    submitButton.setAttribute('disabled', 'disabled');
                }

                showToast('Đang gửi quyết định xử lý...', 'info');
            });
        });
    }

    function bindCharCounters() {
        selectAll('textarea[data-mod-counter]').forEach((textarea) => {
            const counter = textarea.closest('.mod-form')?.querySelector('.mod-char-counter');
            if (!counter) return;

            const max = Number(textarea.getAttribute('maxlength')) || 600;

            const updateCounter = () => {
                const length = textarea.value.length;
                counter.textContent = `${length}/${max}`;
                counter.classList.toggle('text-warning', length > max * 0.8);
            };

            textarea.addEventListener('input', updateCounter);
            updateCounter();
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        bindFormConfirmations();
        bindCharCounters();
    });

    window.modShowToast = showToast;
})();

