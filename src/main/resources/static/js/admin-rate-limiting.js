(() => {
    const AUTO_REFRESH_INTERVAL = 30000;
    let blockModal;
    let whitelistModal;
    let currentIpAddress = '';
    let autoRefreshTimer = null;
    let lastSyncAt = Date.now();
    let requestsChart;

    const statsEndpoint = '/admin/rate-limiting/api/statistics';
    const exportEndpoint = '/admin/rate-limiting/api/export-data';
    const blockEndpoint = '/admin/rate-limiting/api/block-ip';
    const unblockEndpoint = '/admin/rate-limiting/api/unblock-ip';
    const unblockPermanentEndpoint = '/admin/rate-limiting/api/unblock-permanent';
    const editReasonEndpoint = '/admin/rate-limiting/api/edit-block-reason';
    const suspiciousClearEndpoint = '/admin/rate-limiting/api/clear-suspicious-flag';
    const whitelistEndpoint = '/admin/rate-limiting/api/whitelist-ip';
    const clearAllEndpoint = '/admin/rate-limiting/api/clear-all-blocks';
    const resetAllEndpoint = '/admin/rate-limiting/api/reset-all-limits';

    const statElements = () => document.querySelectorAll('.rl-stat-value[data-stat-key]');
    const lastSyncLabel = () => document.getElementById('rl-last-sync');
    const autoRefreshToggle = () => document.getElementById('rl-auto-refresh');

    const data = window.RateLimitingData || {};

    function initModals() {
        const blockModalEl = document.getElementById('blockIpModal');
        const whitelistModalEl = document.getElementById('whitelistModal');
        if (blockModalEl) {
            blockModal = bootstrap.Modal.getOrCreateInstance(blockModalEl);
        }
        if (whitelistModalEl) {
            whitelistModal = bootstrap.Modal.getOrCreateInstance(whitelistModalEl);
        }
    }

    function initCharts() {
        if (typeof Chart === 'undefined') {
            return;
        }

        const ctx = document.getElementById('requestsBreakdownChart');
        if (!ctx) {
            return;
        }

        const overall = data.overallStats || {};
        const successful = Number(overall.successfulRequests || 0);
        const blocked = Number(overall.blockedRequests || 0);

        requestsChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Thành công', 'Bị chặn'],
                datasets: [{
                    data: [successful, blocked],
                    backgroundColor: ['#22c55e', '#ef4444'],
                    hoverOffset: 8,
                    borderWidth: 0
                }]
            },
            options: {
                cutout: '68%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 18,
                            usePointStyle: true,
                            pointStyle: 'circle'
                        }
                    },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        cornerRadius: 12,
                        padding: 12
                    }
                }
            }
        });
    }

    function updateChartDataset(stats) {
        if (!requestsChart) {
            return;
        }
        const successful = Number(stats.successfulRequests || 0);
        const blocked = Number(stats.blockedRequests || 0);
        requestsChart.data.datasets[0].data = [successful, blocked];
        requestsChart.update('none');
    }

    function formatRelativeTime(deltaMillis) {
        const seconds = Math.round(deltaMillis / 1000);
        if (seconds <= 4) {
            return 'Đồng bộ cách đây 0s';
        }
        if (seconds < 60) {
            return `Đồng bộ cách đây ${seconds}s`;
        }
        const minutes = Math.round(seconds / 60);
        if (minutes < 60) {
            return `Đồng bộ ${minutes} phút trước`;
        }
        const hours = Math.round(minutes / 60);
        if (hours < 24) {
            return `Đồng bộ ${hours} giờ trước`;
        }
        const days = Math.round(hours / 24);
        return `Đồng bộ ${days} ngày trước`;
    }

    function updateLastSyncDisplay() {
        const label = lastSyncLabel();
        if (!label) {
            return;
        }
        label.textContent = formatRelativeTime(Date.now() - lastSyncAt);
    }

    function startLastSyncTicker() {
        updateLastSyncDisplay();
        setInterval(updateLastSyncDisplay, 1000);
    }

    function updateStatCards(stats) {
        statElements().forEach((el) => {
            const key = el.getAttribute('data-stat-key');
            if (!key) return;
            const value = stats[key];
            if (value !== undefined && value !== null) {
                el.textContent = value;
            }
        });
    }

    function fetchAndUpdateStats(showToastMessage = false) {
        return fetch(statsEndpoint)
            .then((res) => res.ok ? res.json() : Promise.reject(new Error('Không thể lấy dữ liệu thống kê')))
            .then((stats) => {
                updateStatCards(stats);
                updateChartDataset(stats);
                lastSyncAt = Date.now();
                updateLastSyncDisplay();
                if (showToastMessage) {
                    showToast('Số liệu đã được cập nhật.', 'success');
                }
            })
            .catch((err) => {
                console.error(err);
                if (showToastMessage) {
                    showToast(err.message || 'Không thể cập nhật số liệu.', 'danger');
                }
            });
    }

    function startAutoRefresh() {
        stopAutoRefresh();
        autoRefreshTimer = setInterval(() => fetchAndUpdateStats(false), AUTO_REFRESH_INTERVAL);
    }

    function stopAutoRefresh() {
        if (autoRefreshTimer) {
            clearInterval(autoRefreshTimer);
            autoRefreshTimer = null;
        }
    }

    function initAutoRefreshToggle() {
        const toggle = autoRefreshToggle();
        if (!toggle) {
            return;
        }
        toggle.addEventListener('change', () => {
            if (toggle.checked) {
                showToast('Bắt đầu tự động làm mới dữ liệu.', 'info');
                startAutoRefresh();
                fetchAndUpdateStats(false);
            } else {
                stopAutoRefresh();
                showToast('Tạm dừng tự động làm mới.', 'warning');
            }
        });
        if (toggle.checked) {
            startAutoRefresh();
        }
    }

    function postJson(url, payload) {
        return fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload || {})
        }).then((res) => res.ok ? res.json() : res.json().catch(() => ({})));
    }

    function showToast(message, type = 'info') {
        const container = document.createElement('div');
        const alertClass = {
            success: 'alert-success',
            danger: 'alert-danger',
            warning: 'alert-warning',
            info: 'alert-info'
        }[type] || 'alert-info';

        container.className = `alert ${alertClass} notification-toast fade show`;
        container.innerHTML = `
            <div class="d-flex align-items-center gap-2">
                <i class="fas fa-circle-info"></i>
                <span>${message}</span>
            </div>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;

        document.body.appendChild(container);
        setTimeout(() => {
            container.classList.remove('show');
            container.classList.add('hide');
            setTimeout(() => container.remove(), 300);
        }, 4500);
    }

    // === Global actions ===
    function refreshData() {
        showToast('Đang làm mới dữ liệu...', 'info');
        fetchAndUpdateStats(true).finally(() => {
            setTimeout(() => window.location.reload(), 600);
        });
    }

    function exportData() {
        showToast('Đang chuẩn bị gói dữ liệu...', 'info');
        fetch(exportEndpoint, { method: 'GET', headers: { 'Content-Type': 'application/json' } })
            .then((res) => res.ok ? res.json() : Promise.reject(new Error('Export thất bại')))
            .then((payload) => {
                if (!payload || !payload.success) {
                    throw new Error(payload?.message || 'Export thất bại');
                }
                const blob = new Blob([JSON.stringify(payload.data, null, 2)], { type: 'application/json' });
                const url = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.download = `rate-limiting-${new Date().toISOString().split('T')[0]}.json`;
                link.click();
                window.URL.revokeObjectURL(url);
                showToast(`Đã xuất ${payload.totalRecords} bản ghi.`, 'success');
            })
            .catch((err) => showToast(err.message || 'Export thất bại', 'danger'));
    }

    function viewIpDetails(ipAddress) {
        if (!ipAddress) return;
        showToast(`Đang mở chi tiết cho ${ipAddress}`, 'info');
        setTimeout(() => {
            window.open(`/admin/rate-limiting/ip-details?ip=${encodeURIComponent(ipAddress)}`, '_blank');
        }, 200);
    }

    function blockIp(ipAddress) {
        currentIpAddress = ipAddress;
        const ipField = document.getElementById('blockIpAddress');
        const reasonField = document.getElementById('blockReason');
        const notesField = document.getElementById('blockNotes');
        if (ipField) ipField.value = ipAddress;
        if (reasonField) reasonField.value = '';
        if (notesField) notesField.value = '';
        blockModal?.show();
    }

    function confirmBlockIp() {
        const reason = document.getElementById('blockReason')?.value;
        const notes = document.getElementById('blockNotes')?.value;
        if (!reason) {
            showToast('Vui lòng chọn lý do.', 'warning');
            return;
        }
        showToast(`Đang chặn IP ${currentIpAddress}...`, 'info');
        postJson(blockEndpoint, { ipAddress: currentIpAddress, reason, notes })
            .then((res) => {
                blockModal?.hide();
                showToast(res?.message || 'Đã chặn IP.', res?.success ? 'success' : 'danger');
                if (res?.success) setTimeout(() => window.location.reload(), 800);
            })
            .catch((err) => showToast(err.message || 'Lỗi chặn IP.', 'danger'));
    }

    function unblockIp(ipAddress) {
        if (!confirm(`Bỏ chặn IP ${ipAddress}?`)) return;
        showToast(`Đang bỏ chặn ${ipAddress}...`, 'info');
        postJson(unblockEndpoint, { ipAddress })
            .then((res) => {
                showToast(res?.message || 'Đã bỏ chặn.', res?.success ? 'success' : 'danger');
                if (res?.success) setTimeout(() => window.location.reload(), 800);
            })
            .catch((err) => showToast(err.message || 'Không thể bỏ chặn.', 'danger'));
    }

    function unblockPermanent(ipAddress) {
        if (!confirm(`Xoá block vĩnh viễn cho ${ipAddress}?`)) return;
        showToast('Đang xử lý...', 'info');
        postJson(unblockPermanentEndpoint, { ipAddress })
            .then((res) => {
                showToast(res?.message || 'Đã xoá block.', res?.success ? 'success' : 'danger');
                if (res?.success) setTimeout(() => window.location.reload(), 800);
            })
            .catch((err) => showToast(err.message || 'Không thể xoá block.', 'danger'));
    }

    function editBlockReason(ipAddress) {
        const newReason = prompt(`Cập nhật lý do chặn cho ${ipAddress}:`, 'Updated reason');
        if (!newReason) return;
        showToast('Đang cập nhật...', 'info');
        postJson(editReasonEndpoint, { ipAddress, newReason })
            .then((res) => showToast(res?.message || 'Đã cập nhật lý do.', res?.success ? 'success' : 'danger'))
            .catch((err) => showToast(err.message || 'Không thể cập nhật lý do.', 'danger'));
    }

    function blockSuspiciousIp(ipAddress) {
        const reason = prompt(`Chặn IP đáng ngờ ${ipAddress} vì...`, 'Suspicious activity');
        if (!reason) return;
        showToast('Đang chặn IP...', 'info');
        postJson(blockEndpoint, { ipAddress, reason })
            .then((res) => {
                showToast(res?.message || 'Đã chặn IP.', res?.success ? 'success' : 'danger');
                if (res?.success) setTimeout(() => window.location.reload(), 800);
            })
            .catch((err) => showToast(err.message || 'Không thể chặn IP.', 'danger'));
    }

    function clearSuspiciousFlag(ipAddress) {
        if (!confirm(`Xoá cờ đáng ngờ cho ${ipAddress}?`)) return;
        showToast('Đang xoá cờ...', 'info');
        postJson(suspiciousClearEndpoint, { ipAddress })
            .then((res) => {
                showToast(res?.message || 'Đã xoá cờ.', res?.success ? 'success' : 'danger');
                if (res?.success) setTimeout(() => window.location.reload(), 800);
            })
            .catch((err) => showToast(err.message || 'Không thể xoá cờ.', 'danger'));
    }

    function clearAllBlocks() {
        if (!confirm('Xoá toàn bộ block? Hành động này không thể hoàn tác.')) return;
        showToast('Đang xoá toàn bộ block...', 'warning');
        postJson(clearAllEndpoint)
            .then((res) => {
                showToast(res?.message || 'Đã xoá block.', res?.success ? 'success' : 'danger');
                if (res?.success) setTimeout(() => window.location.reload(), 800);
            })
            .catch((err) => showToast(err.message || 'Không thể xoá block.', 'danger'));
    }

    function resetAllLimits() {
        if (!confirm('Reset counters cho toàn bộ IP?')) return;
        showToast('Đang reset giới hạn...', 'warning');
        postJson(resetAllEndpoint)
            .then((res) => {
                showToast(res?.message || 'Đã reset giới hạn.', res?.success ? 'success' : 'danger');
                if (res?.success) setTimeout(() => window.location.reload(), 800);
            })
            .catch((err) => showToast(err.message || 'Không thể reset giới hạn.', 'danger'));
    }

    function addWhitelistIp() {
        document.getElementById('whitelistForm')?.reset();
        whitelistModal?.show();
    }

    function confirmAddWhitelist() {
        const ipAddress = document.getElementById('whitelistIpAddress')?.value;
        const description = document.getElementById('whitelistDescription')?.value;
        if (!ipAddress) {
            showToast('Vui lòng nhập IP.', 'warning');
            return;
        }
        showToast('Đang thêm whitelist...', 'info');
        postJson(whitelistEndpoint, { ipAddress, description })
            .then((res) => {
                whitelistModal?.hide();
                showToast(res?.message || 'Đã cập nhật whitelist.', res?.success ? 'success' : 'danger');
            })
            .catch((err) => showToast(err.message || 'Không thể cập nhật whitelist.', 'danger'));
    }

    function blockIpManual() {
        const ip = prompt('Nhập IP cần chặn:', '192.168.1.100');
        if (!ip) return;
        blockIp(ip);
    }

    function setup() {
        initModals();
        initCharts();
        startLastSyncTicker();
        initAutoRefreshToggle();
    }

    document.addEventListener('DOMContentLoaded', setup);

    // expose
    window.refreshData = refreshData;
    window.exportData = exportData;
    window.viewIpDetails = viewIpDetails;
    window.blockIp = blockIp;
    window.confirmBlockIp = confirmBlockIp;
    window.unblockIp = unblockIp;
    window.unblockPermanent = unblockPermanent;
    window.editBlockReason = editBlockReason;
    window.blockSuspiciousIp = blockSuspiciousIp;
    window.clearSuspiciousFlag = clearSuspiciousFlag;
    window.clearAllBlocks = clearAllBlocks;
    window.resetAllLimits = resetAllLimits;
    window.addWhitelistIp = addWhitelistIp;
    window.confirmAddWhitelist = confirmAddWhitelist;
    window.blockIpManual = blockIpManual;
    window.showToast = showToast;
})();

