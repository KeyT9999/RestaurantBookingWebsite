(function () {
  document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('aiSearchForm');
    if (!form) {
      return;
    }

    const queryInput = document.getElementById('aiSearchQuery');
    const maxResultsInput = document.getElementById('aiMaxResults');
    const statusEl = document.getElementById('aiSearchStatus');
    const panelEl = document.getElementById('aiResultsPanel');
    const summaryEl = document.getElementById('aiResultsSummary');
    const explanationEl = document.getElementById('aiResultsExplanation');
    const confidenceEl = document.getElementById('aiResultsConfidence');
    const confidenceValueEl = confidenceEl ? confidenceEl.querySelector('span') : null;
    const recommendationsEl = document.getElementById('aiRecommendations');
    const emptyStateEl = document.getElementById('aiEmptyState');
    const interpretationBoxEl = document.getElementById('aiInterpretationBox');
    const interpretationTextEl = document.getElementById('aiInterpretationText');
    const suggestedFoodsEl = document.getElementById('aiSuggestedFoods');
    const suggestedFoodsListEl = document.getElementById('aiSuggestedFoodsList');

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content') || null;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

    const setStatus = (message, options = {}) => {
      const { state = 'info', loading = false } = options;
      if (!statusEl) {
        return;
      }

      statusEl.classList.toggle('error', state === 'error');

      if (loading) {
        statusEl.innerHTML = `<span class="ai-loading-spinner">${message}</span>`;
        return;
      }

      statusEl.textContent = message;
    };

    const resetResults = () => {
      if (panelEl) {
        panelEl.hidden = true;
      }
      if (summaryEl) {
        summaryEl.textContent = '';
      }
      if (explanationEl) {
        explanationEl.textContent = '';
      }
      if (confidenceEl) {
        confidenceEl.hidden = true;
        if (confidenceValueEl) {
          confidenceValueEl.textContent = '';
        }
      }
      if (recommendationsEl) {
        recommendationsEl.innerHTML = '';
      }
      if (emptyStateEl) {
        emptyStateEl.hidden = true;
      }
      if (interpretationBoxEl) {
        interpretationBoxEl.style.display = 'none';
      }
      if (interpretationTextEl) {
        interpretationTextEl.textContent = '';
      }
      if (suggestedFoodsEl) {
        suggestedFoodsEl.style.display = 'none';
      }
      if (suggestedFoodsListEl) {
        suggestedFoodsListEl.innerHTML = '';
      }
    };

    const clamp = (value, min, max) => Math.min(Math.max(value, min), max);

    const buildRecommendationCard = (rec) => {
      const card = document.createElement('article');
      card.className = 'ai-recommendation-card';

      const header = document.createElement('div');
      header.className = 'ai-rec-header';
      card.appendChild(header);

      const name = document.createElement('h3');
      name.className = 'ai-rec-name';
      name.textContent = rec.restaurantName || 'Nhà hàng không tên';
      header.appendChild(name);

      if (rec.rating && rec.rating !== '0.0') {
        const rating = document.createElement('span');
        rating.className = 'ai-rec-rating';
        rating.innerHTML = `<i class="fas fa-star"></i>${rec.rating}`;
        header.appendChild(rating);
      }

      const meta = document.createElement('div');
      meta.className = 'ai-rec-meta';
      const metaItems = [];
      if (rec.cuisineType) {
        metaItems.push(`<i class="fas fa-utensils"></i> ${rec.cuisineType}`);
      }
      if (rec.priceRange) {
        metaItems.push(`<i class="fas fa-money-bill-wave"></i> ${rec.priceRange}`);
      }
      if (typeof rec.distanceKm === 'number') {
        metaItems.push(`<i class="fas fa-location-arrow"></i> ${rec.distanceKm.toFixed(1)} km`);
      }
      if (metaItems.length > 0) {
        meta.innerHTML = metaItems.join(' · ');
        card.appendChild(meta);
      }

      const actions = document.createElement('div');
      actions.className = 'ai-rec-actions';

      if (rec.bookingUrl) {
        const bookLink = document.createElement('a');
        bookLink.className = 'primary';
        bookLink.href = rec.bookingUrl;
        bookLink.textContent = 'Đặt bàn ngay';
        actions.appendChild(bookLink);
      }

      if (rec.viewDetailsUrl) {
        const detailLink = document.createElement('a');
        detailLink.className = 'secondary';
        detailLink.href = rec.viewDetailsUrl;
        detailLink.textContent = 'Xem chi tiết';
        actions.appendChild(detailLink);
      }

      if (actions.children.length > 0) {
        card.appendChild(actions);
      }

      return card;
    };

    form.addEventListener('submit', async (event) => {
      event.preventDefault();

      const rawQuery = queryInput ? queryInput.value.trim() : '';
      if (rawQuery.length < 5) {
        setStatus('Vui lòng mô tả mong muốn cụ thể hơn (tối thiểu 5 ký tự).', { state: 'error' });
        resetResults();
        return;
      }

      let maxResults = parseInt(maxResultsInput?.value, 10);
      if (Number.isFinite(maxResults)) {
        maxResults = clamp(maxResults, 1, 10);
        maxResultsInput.value = String(maxResults);
      } else {
        maxResults = undefined;
      }

      setStatus('Đang phân tích yêu cầu của bạn...', { loading: true });
      resetResults();

      const payload = {
        query: rawQuery,
        maxResults,
        language: 'vi',
        includeContext: true,
        userTimezone: Intl.DateTimeFormat().resolvedOptions().timeZone || undefined,
        deviceType: window.navigator.userAgent
      };

      if (!payload.maxResults) {
        delete payload.maxResults;
      }

      try {
        const headers = {
          'Content-Type': 'application/json'
        };
        if (csrfToken) {
          headers[csrfHeader] = csrfToken;
        }

        const response = await fetch('/ai/search', {
          method: 'POST',
          headers,
          body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (!data) {
          throw new Error('Không nhận được phản hồi từ máy chủ.');
        }

        // Debug: Log response data
        console.log('🔍 AI Search Response:', data);
        console.log('📊 AI Interpretation:', data.aiInterpretation);
        console.log('🍽️ Suggested Foods:', data.suggestedFoods);

        const totalFound = Number.isFinite(data.totalFound) ? data.totalFound : 0;
        const totalReturned = Number.isFinite(data.totalReturned) ? data.totalReturned : (Array.isArray(data.recommendations) ? data.recommendations.length : 0);

        if (summaryEl) {
          const queryText = data.originalQuery || rawQuery;
          summaryEl.textContent = `Gợi ý cho "${queryText}" — ${totalReturned}/${totalFound} nhà hàng được trả về.`;
        }

        // Display AI Interpretation if available
        if (interpretationBoxEl && interpretationTextEl) {
          const aiInterpretation = data.aiInterpretation || '';
          console.log('🎨 Checking AI Interpretation:', {
            hasBox: !!interpretationBoxEl,
            hasText: !!interpretationTextEl,
            interpretation: aiInterpretation,
            isEmpty: !aiInterpretation || aiInterpretation.trim() === ''
          });
          
          if (aiInterpretation && aiInterpretation.trim() !== '') {
            console.log('✅ Displaying AI Interpretation:', aiInterpretation);
            interpretationTextEl.textContent = aiInterpretation;
            interpretationBoxEl.style.display = 'block';
            
            // Display suggested foods if available
            if (suggestedFoodsEl && suggestedFoodsListEl && data.suggestedFoods && Array.isArray(data.suggestedFoods) && data.suggestedFoods.length > 0) {
              console.log('🍽️ Displaying Suggested Foods:', data.suggestedFoods);
              suggestedFoodsListEl.innerHTML = '';
              data.suggestedFoods.forEach((food) => {
                if (food && food.trim() !== '') {
                  const badge = document.createElement('span');
                  badge.style.cssText = 'background: rgba(255, 255, 255, 0.25); color: white; padding: 6px 12px; border-radius: 20px; font-size: 0.9rem; font-weight: 500; display: inline-flex; align-items: center; gap: 6px;';
                  badge.innerHTML = `<i class="fas fa-utensils"></i>${food}`;
                  suggestedFoodsListEl.appendChild(badge);
                }
              });
              if (suggestedFoodsListEl.children.length > 0) {
                suggestedFoodsEl.style.display = 'block';
              }
            } else {
              console.log('⚠️ No suggested foods to display');
            }
          } else {
            console.log('❌ AI Interpretation is empty, hiding box');
            interpretationBoxEl.style.display = 'none';
          }
        } else {
          console.error('❌ Missing interpretation elements:', {
            interpretationBoxEl: !!interpretationBoxEl,
            interpretationTextEl: !!interpretationTextEl
          });
        }

        if (explanationEl) {
          explanationEl.textContent = data.explanation || '';
        }

        if (confidenceEl && confidenceValueEl) {
          const confidenceScore = parseFloat(data.confidenceScore);
          if (!Number.isNaN(confidenceScore)) {
            confidenceValueEl.textContent = `Độ tự tin ${Math.round(confidenceScore * 100)}%`;
            confidenceEl.hidden = false;
          } else {
            confidenceEl.hidden = true;
          }
        }

        const items = Array.isArray(data.recommendations) ? data.recommendations : [];
        if (recommendationsEl) {
          recommendationsEl.innerHTML = '';
          items.forEach((rec) => {
            recommendationsEl.appendChild(buildRecommendationCard(rec));
          });
        }

        if (emptyStateEl) {
          emptyStateEl.hidden = items.length > 0;
        }

        if (panelEl) {
          panelEl.hidden = false;
        }

        const fallbackExplanation = 'Tìm thấy nhà hàng phù hợp (chế độ đơn giản)';
        const usedFallback = (data.explanation || '').trim() === fallbackExplanation;
        setStatus(usedFallback ? 'Hiển thị kết quả ở chế độ đơn giản.' : 'Đã cập nhật gợi ý phù hợp.');
      } catch (error) {
        console.error('AI search failed', error);
        setStatus(error.message || 'Không thể kết nối tới máy chủ. Vui lòng thử lại.', { state: 'error' });
        resetResults();
      }
    });
  });
})(); 
