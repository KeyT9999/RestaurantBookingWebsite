/**
 * Customer Chat JavaScript
 * Handles WebSocket communication and UI interactions for customer chat
 */

class CustomerChatManager {
  constructor() {
    this.socket = null;
    this.stompClient = null;
    this.currentRoomId = null;
    this.currentUserId = null;
    this.userRole = null;
    this.isConnected = false;
    this.typingTimer = null;
    this.reconnectAttempts = 0;
    
    // Infinite scroll properties
    this.currentPage = 0;
    this.pageSize = 100;
    this.hasMoreMessages = true;
    this.isLoadingMessages = false;
    this.allMessages = [];

    this.init();
  }

  init() {
    this.loadUserInfo();
    this.initWebSocket();
    this.setupEventListeners();
    this.setupRoomClickHandlers();
    this.setupInfiniteScroll();
    this.loadAllRoomLogos();
  }

  // Load user information from server
  async loadUserInfo() {
    try {
      const response = await fetch("/api/user/current");
      if (response.ok) {
        const user = await response.json();
        this.currentUserId = user.id;
        this.userRole = user.role;
      }
    } catch (error) {
      console.error("Failed to load user info:", error);
    }
  }

  // Initialize WebSocket connection
  initWebSocket() {
    try {
      this.socket = new SockJS("/ws");
      this.stompClient = Stomp.over(this.socket);

      this.stompClient.connect(
        {},
        (frame) => {
          console.log("Connected to WebSocket");
          this.isConnected = true;
          this.resetReconnectAttempts();
          this.subscribeToMessages();
        },
        (error) => {
          console.error("WebSocket connection error:", error);
          this.isConnected = false;
          this.reconnectWebSocket();
        }
      );
    } catch (error) {
      console.error("Failed to initialize WebSocket:", error);
    }
  }

  // Subscribe to messages
  subscribeToMessages() {
    if (!this.isConnected) return;

    // Subscribe to room messages
    this.stompClient.subscribe("/topic/room/*", (message) => {
      const data = JSON.parse(message.body);
      this.handleIncomingMessage(data);
    });

    // Subscribe to typing indicators
    this.stompClient.subscribe("/topic/room/*/typing", (message) => {
      const data = JSON.parse(message.body);
      this.handleTypingIndicator(data);
    });

    // Subscribe to unread count updates
    this.stompClient.subscribe("/user/queue/unread-updates", (message) => {
      const data = JSON.parse(message.body);
      console.log("Customer received unread update:", data);
      handleUnreadCountUpdate(data);
    });

    // Subscribe to user-specific errors
    this.stompClient.subscribe("/user/queue/errors", (message) => {
      const data = JSON.parse(message.body);
      this.showError(data.message);
    });
  }

  // Reconnect WebSocket with exponential backoff
  reconnectWebSocket() {
    const maxRetries = 5;
    const baseDelay = 1000;

    if (this.reconnectAttempts >= maxRetries) {
      console.error("Max reconnection attempts reached");
      this.showError("Không thể kết nối lại. Vui lòng tải lại trang.");
      return;
    }

    this.reconnectAttempts++;
    const delay = baseDelay * Math.pow(2, this.reconnectAttempts - 1);

    console.log(
      `Attempting to reconnect WebSocket (attempt ${this.reconnectAttempts}/${maxRetries}) in ${delay}ms...`
    );

    setTimeout(() => {
      this.initWebSocket();
    }, delay);
  }

  resetReconnectAttempts() {
    this.reconnectAttempts = 0;
  }

  // Setup event listeners
  setupEventListeners() {
    // Message input
    const messageInput = document.getElementById("message-input");
    const sendButton = document.getElementById("send-button");

    if (messageInput) {
      messageInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
          e.preventDefault();
          this.sendMessage();
        }
      });

      messageInput.addEventListener("input", () => {
        this.handleTyping();
      });
    }

    if (sendButton) {
      sendButton.addEventListener("click", () => {
        this.sendMessage();
      });
    }

    // Back to list button
    const backButton = document.getElementById("back-to-list");
    if (backButton) {
      backButton.addEventListener("click", () => {
        this.showWelcomeScreen();
      });
    }

    // Mobile sidebar toggle
    const sidebarToggle = document.getElementById("sidebar-toggle");
    if (sidebarToggle) {
      sidebarToggle.addEventListener("click", () => {
        this.toggleSidebar();
      });
    }
  }

  // Setup room click handlers
  setupRoomClickHandlers() {
    document.addEventListener("click", (e) => {
      // Handle AI Assistant item
      const aiAssistantItem = e.target.closest(".ai-assistant-room-item");
      if (aiAssistantItem) {
        e.preventDefault();
        e.stopPropagation();
        this.startChatWithAI();
        return;
      }
      
      // Handle chat room items
      const roomItem = e.target.closest(".chat-room-item");
      if (roomItem) {
        const roomId = roomItem.dataset.roomId;
        if (roomId) {
          e.preventDefault();
          e.stopPropagation();
          this.joinRoom(roomId);
        }
      }
    });
  }

  // Load available restaurants
  async loadAvailableRestaurants() {
    try {
      const response = await fetch("/api/chat/available-restaurants");
      if (response.ok) {
        const restaurants = await response.json();
        this.displayRestaurants(restaurants);
      } else {
        console.error("Failed to load restaurants");
      }
    } catch (error) {
      console.error("Error loading restaurants:", error);
    }
  }

  // Display restaurants in sidebar
  displayRestaurants(restaurants) {
    const restaurantList = document.getElementById("restaurant-list");
    if (!restaurantList) return;

    restaurantList.innerHTML = "";

    if (restaurants.length === 0) {
      restaurantList.innerHTML =
        '<div class="loading-state"><small>Không có nhà hàng nào</small></div>';
      return;
    }

    // Separate AI restaurant from others
    const aiRestaurant = restaurants.find((r) => r.restaurantId === 37);
    const otherRestaurants = restaurants.filter((r) => r.restaurantId !== 37);

    // AI Assistant is already in HTML, just update the button handler
    const aiChatBtn = document.getElementById("start-ai-chat-btn");
    if (aiChatBtn && aiRestaurant) {
      // Remove existing listeners by cloning
      const newBtn = aiChatBtn.cloneNode(true);
      aiChatBtn.parentNode.replaceChild(newBtn, aiChatBtn);
      
      // Add new listener
      newBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        this.startChatWithAI();
      });
    }

    // Add other restaurants
    otherRestaurants.forEach((restaurant) => {
      const restaurantItem = this.createRestaurantItem(restaurant);
      restaurantList.appendChild(restaurantItem);
    });
  }

  // AI Assistant is now in HTML template, this method is kept for compatibility
  createAIContainer(restaurant) {
    // AI Assistant entry is already in HTML, just return null
    return null;
  }

  // Create restaurant item element
  createRestaurantItem(restaurant) {
    const template = document.getElementById("restaurant-item-template");
    const item = template.content.cloneNode(true);

    const restaurantEntry = item.querySelector(".restaurant-entry");
    const nameElement = item.querySelector(".restaurant-name");
    const addressElement = item.querySelector(".restaurant-address");
    const startChatBtn = item.querySelector(".start-chat-btn");

    restaurantEntry.dataset.restaurantId = restaurant.restaurantId;
    // Add room ID if available
    if (restaurant.roomId) {
      restaurantEntry.dataset.roomId = restaurant.roomId;
    }

    nameElement.textContent = restaurant.restaurantName || "Nhà hàng";
    addressElement.textContent = restaurant.address || "Địa chỉ không xác định";

    startChatBtn.addEventListener("click", (e) => {
      e.stopPropagation();
      this.startChatWithRestaurant(restaurant);
    });

    // Also make the whole entry clickable
    restaurantEntry.addEventListener("click", (e) => {
      if (e.target !== startChatBtn) {
        this.startChatWithRestaurant(restaurant);
      }
    });

    return item;
  }

  // Start chat with AI
  async startChatWithAI() {
    try {
      // Update header immediately for AI
      const participantNameElement = document.getElementById("participant-name");
      const addressElement = document.getElementById("participant-address");
      const participantIcon = document.getElementById("participant-icon");
      
      if (participantNameElement) {
        participantNameElement.textContent = "Trợ lý AI";
      }
      if (addressElement) {
        addressElement.textContent = "Trợ lý thông minh của bạn";
        addressElement.style.display = "block";
      }
      if (participantIcon) {
        participantIcon.className = "fas fa-robot";
      }
      
      const response = await fetch("/api/chat/rooms", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `restaurantId=37`, // AI restaurant ID
      });

      if (response.ok) {
        const room = await response.json();
        this.joinRoom(room.roomId);
      } else {
        const error = await response.json();
        this.showError(error.message || "Không thể tạo cuộc trò chuyện với AI");
      }
    } catch (error) {
      console.error("Error starting chat with AI:", error);
      this.showError("Lỗi khi tạo cuộc trò chuyện với AI");
    }
  }

  // Start chat with restaurant
  async startChatWithRestaurant(restaurant) {
    try {
      const response = await fetch("/api/chat/rooms", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `restaurantId=${restaurant.restaurantId}`,
      });

      if (response.ok) {
        const room = await response.json();
        this.joinRoom(room.roomId);
      } else {
        const error = await response.json();
        this.showError(error.message || "Không thể tạo cuộc trò chuyện");
      }
    } catch (error) {
      console.error("Error starting chat with restaurant:", error);
      this.showError("Lỗi khi tạo cuộc trò chuyện");
    }
  }

  // Join a chat room
  async joinRoom(roomId) {
    try {
      // Set current room ID first
      this.currentRoomId = roomId;

      // Show loading state
      this.showLoadingState();

      // Load room info to get restaurant name
      await this.loadRoomInfo(roomId);

      // Join room via WebSocket
      if (this.isConnected) {
        console.log("🔗 Joining room via WebSocket:", roomId);
        this.stompClient.send(
          "/app/chat/join",
          {},
          JSON.stringify({
            roomId: roomId,
          })
        );
      } else {
        console.warn("⚠️ WebSocket not connected, cannot join room");
      }

      // Reset pagination for new room
      this.currentPage = 0;
      this.hasMoreMessages = true;
      this.allMessages = [];
      
      // Load room messages
      await this.loadMessages(roomId);
      this.showChatInterface();
      this.updateRoomSelection(roomId);

      // Mark messages as read after loading
      await this.markMessagesAsRead(roomId);
    } catch (error) {
      console.error("Error joining room:", error);
      this.showError("Lỗi khi tham gia cuộc trò chuyện");
    }
  }

  // Load room info to display restaurant name and address
  async loadRoomInfo(roomId) {
    try {
      // First try to get info from room item in sidebar
      const roomItem = document.querySelector(`[data-room-id="${roomId}"]`);
      if (roomItem) {
        const roomNameElement = roomItem.querySelector(".room-name");
        const restaurantId = roomItem.dataset.restaurantId;
        
        if (roomNameElement) {
          const participantNameElement = document.getElementById("participant-name");
          if (participantNameElement) {
            participantNameElement.textContent = roomNameElement.textContent;
          }
          
          // Try to load restaurant address and logo from API
          if (restaurantId) {
            try {
              const restaurantResponse = await fetch(`/api/booking/restaurants/${restaurantId}`);
              if (restaurantResponse.ok) {
                const restaurant = await restaurantResponse.json();
                const addressElement = document.getElementById("participant-address");
                if (addressElement && restaurant.address) {
                  addressElement.textContent = restaurant.address;
                  addressElement.style.display = "block";
                }
                
                // Load restaurant logo
                await this.loadRestaurantLogo(restaurantId, roomItem);
              }
            } catch (error) {
              console.error("Error loading restaurant info:", error);
            }
          }
        }
      }
      
      // Also try to get from API if available
      try {
        const response = await fetch(`/api/chat/rooms/${roomId}`);
        if (response.ok) {
          const roomData = await response.json();
          // Update restaurant name in header
          const participantNameElement = document.getElementById("participant-name");
          if (participantNameElement && roomData.restaurantName) {
            participantNameElement.textContent = roomData.restaurantName;
          }
          
          // Try to get restaurant address and logo
          if (roomData.restaurantId) {
            try {
              const restaurantResponse = await fetch(`/api/booking/restaurants/${roomData.restaurantId}`);
              if (restaurantResponse.ok) {
                const restaurant = await restaurantResponse.json();
                const addressElement = document.getElementById("participant-address");
                if (addressElement && restaurant.address) {
                  addressElement.textContent = restaurant.address;
                  addressElement.style.display = "block";
                }
                
                // Load restaurant logo
                const roomItem = document.querySelector(`[data-room-id="${roomId}"]`);
                if (roomItem) {
                  await this.loadRestaurantLogo(roomData.restaurantId, roomItem);
                }
              }
            } catch (error) {
              console.error("Error loading restaurant info:", error);
            }
          }
          
          // Update icon if needed
          const participantIcon = document.getElementById("participant-icon");
          if (participantIcon) {
            if (roomData.restaurantId === 37) {
              participantIcon.className = "fas fa-robot";
            } else if (roomData.participantRole === 'ADMIN') {
              participantIcon.className = "fas fa-user-shield";
            } else {
              participantIcon.className = "fas fa-store";
            }
          }
        }
      } catch (error) {
        console.error("Error loading room info from API:", error);
      }
    } catch (error) {
      console.error("Error loading room info:", error);
    }
  }

  // Load logos for all room items
  async loadAllRoomLogos() {
    const roomItems = document.querySelectorAll('.chat-room-item[data-restaurant-id]');
    for (const roomItem of roomItems) {
      const restaurantId = roomItem.dataset.restaurantId;
      if (restaurantId) {
        await this.loadRestaurantLogo(restaurantId, roomItem);
      }
    }
  }

  // Load restaurant logo for room item
  async loadRestaurantLogo(restaurantId, roomItem) {
    try {
      // Try to get logo from logo API
      const logoResponse = await fetch(`/api/booking/restaurants/${restaurantId}/logo`);
      if (logoResponse.ok) {
        const logoData = await logoResponse.json();
        if (logoData.logoUrl) {
          const avatarElement = roomItem.querySelector('.room-avatar');
          if (avatarElement) {
            const existingImg = avatarElement.querySelector('.avatar-image');
            const fallback = avatarElement.querySelector('.avatar-fallback');
            if (existingImg) {
              existingImg.src = logoData.logoUrl;
              existingImg.style.display = 'block';
              if (fallback) fallback.style.display = 'none';
            } else {
              // Create new img element
              const img = document.createElement('img');
              img.src = logoData.logoUrl;
              img.className = 'avatar-image';
              img.alt = 'Restaurant Logo';
              img.onerror = function() {
                this.style.display = 'none';
                if (fallback) fallback.style.display = 'flex';
              };
              if (fallback) {
                fallback.style.display = 'none';
                avatarElement.insertBefore(img, fallback);
              } else {
                avatarElement.appendChild(img);
              }
            }
          }
        }
      }
    } catch (error) {
      console.error("Error loading restaurant logo:", error);
    }
  }

  // Show loading state
  showLoadingState() {
    const messagesContainer = document.getElementById("messages-container");
    if (messagesContainer) {
      messagesContainer.innerHTML = `
                <div class="text-center text-muted">
                    <div class="spinner-border spinner-border-sm text-gold" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <small>Đang tải...</small>
                </div>
            `;
    }
  }

  // Display messages
  displayMessages(messages) {
    const messagesContainer = document.getElementById("messages-container");
    if (!messagesContainer) return;

    messagesContainer.innerHTML = "";

    if (messages.length === 0) {
      messagesContainer.innerHTML = `
                <div class="text-center text-muted">
                    <i class="fas fa-comments"></i>
                    <p>Chưa có tin nhắn nào</p>
                </div>
            `;
      return;
    }

    messages.forEach((message) => {
      const messageElement = this.createMessageElement(message);
      messagesContainer.appendChild(messageElement);
    });

    // Scroll to bottom
    this.scrollToBottom();
  }

  // Setup infinite scroll listener
  setupInfiniteScroll() {
    const messagesContainer = document.getElementById("messages-container");
    if (!messagesContainer) return;

    messagesContainer.addEventListener('scroll', () => {
      if (messagesContainer.scrollTop === 0 && this.hasMoreMessages && !this.isLoadingMessages) {
        this.loadMoreMessages();
      }
    });
  }

  // Load messages for current room
  async loadMessages(roomId) {
    if (this.isLoadingMessages) return;
    
    this.isLoadingMessages = true;
    this.showLoadingState();

    try {
      const response = await fetch(`/api/chat/rooms/${roomId}/messages?page=${this.currentPage}&size=${this.pageSize}`);
      if (response.ok) {
        const messages = await response.json();
        
        if (this.currentPage === 0) {
          // First load - replace all messages
          this.allMessages = messages;
          this.displayMessages(messages);
        } else {
          // Load more - prepend to existing messages
          this.allMessages = [...messages, ...this.allMessages];
          this.prependMessages(messages);
        }
        
        // Check if there are more messages
        this.hasMoreMessages = messages.length === this.pageSize;
        this.currentPage++;
        
      } else {
        this.showError("Không thể tải tin nhắn");
      }
    } catch (error) {
      console.error("Error loading messages:", error);
      this.showError("Lỗi khi tải tin nhắn");
    } finally {
      this.isLoadingMessages = false;
    }
  }

  // Load more messages (infinite scroll)
  async loadMoreMessages() {
    if (!this.currentRoomId || this.isLoadingMessages || !this.hasMoreMessages) return;
    
    this.isLoadingMessages = true;
    this.showLoadMoreIndicator();

    try {
      const response = await fetch(`/api/chat/rooms/${this.currentRoomId}/messages?page=${this.currentPage}&size=${this.pageSize}`);
      if (response.ok) {
        const messages = await response.json();
        
        if (messages.length > 0) {
          // Store current scroll position
          const messagesContainer = document.getElementById("messages-container");
          const scrollHeight = messagesContainer.scrollHeight;
          
          // Prepend new messages
          this.allMessages = [...messages, ...this.allMessages];
          this.prependMessages(messages);
          
          // Restore scroll position
          const newScrollHeight = messagesContainer.scrollHeight;
          messagesContainer.scrollTop = newScrollHeight - scrollHeight;
        }
        
        // Check if there are more messages
        this.hasMoreMessages = messages.length === this.pageSize;
        this.currentPage++;
        
      }
    } catch (error) {
      console.error("Error loading more messages:", error);
    } finally {
      this.isLoadingMessages = false;
      this.hideLoadMoreIndicator();
    }
  }

  // Prepend messages to the top of the container
  prependMessages(messages) {
    const messagesContainer = document.getElementById("messages-container");
    if (!messagesContainer) return;

    messages.reverse().forEach((message) => {
      const messageElement = this.createMessageElement(message);
      messagesContainer.insertBefore(messageElement, messagesContainer.firstChild);
    });
  }

  // Show load more indicator
  showLoadMoreIndicator() {
    const messagesContainer = document.getElementById("messages-container");
    if (!messagesContainer) return;

    const loadMoreIndicator = document.createElement('div');
    loadMoreIndicator.id = 'load-more-indicator';
    loadMoreIndicator.className = 'text-center text-muted py-2';
    loadMoreIndicator.innerHTML = `
      <div class="spinner-border spinner-border-sm text-gold" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <small>Đang tải thêm tin nhắn...</small>
    `;
    
    messagesContainer.insertBefore(loadMoreIndicator, messagesContainer.firstChild);
  }

  // Hide load more indicator
  hideLoadMoreIndicator() {
    const loadMoreIndicator = document.getElementById('load-more-indicator');
    if (loadMoreIndicator) {
      loadMoreIndicator.remove();
    }
  }

  // Create message element
  createMessageElement(message) {
    const template = document.getElementById("message-template");
    const messageElement = template.content.cloneNode(true);

    const messageItem = messageElement.querySelector(".message-item");
    const senderElement = messageElement.querySelector(".message-sender");
    const timeElement = messageElement.querySelector(".message-time");
    const textElement = messageElement.querySelector(".message-text");

    messageItem.dataset.messageId = message.messageId;

    // Check if message is from current user
    const isOwnMessage = message.senderId === this.currentUserId;
    if (isOwnMessage) {
      messageItem.classList.add("own-message");
    }

    // Set message content - use innerHTML since content is sanitized on server
    senderElement.textContent = message.senderName || "Người dùng";
    timeElement.textContent = this.formatTime(message.sentAt);
    textElement.innerHTML = message.content || "";

    return messageElement;
  }

  // Format time
  formatTime(timeString) {
    if (!timeString) return "Thời gian không xác định";

    try {
      const date = new Date(timeString);
      if (isNaN(date.getTime())) return "Thời gian không xác định";

      return date.toLocaleTimeString("vi-VN", {
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch (error) {
      return "Thời gian không xác định";
    }
  }

  // Send message
  async sendMessage() {
    const messageInput = document.getElementById("message-input");
    if (!messageInput) {
      console.error("Message input not found");
      return;
    }

    if (!this.currentRoomId) {
      console.error("No current room ID");
      this.showError("Chưa chọn cuộc trò chuyện");
      return;
    }

    const content = messageInput.value.trim();
    if (!content) {
      console.log("Empty message content");
      return;
    }

    // Clear input
    messageInput.value = "";

    try {
      // Send via WebSocket
      if (this.isConnected) {
        const messageData = {
          roomId: this.currentRoomId,
          content: content,
        };

        this.stompClient.send(
          "/app/chat.sendMessage",
          {},
          JSON.stringify(messageData)
        );
      } else {
        console.error("WebSocket not connected");
        this.showError("Không có kết nối. Vui lòng thử lại.");
      }
    } catch (error) {
      console.error("Error sending message:", error);
      this.showError("Lỗi khi gửi tin nhắn");
    }
  }

  // Handle incoming message
  handleIncomingMessage(data) {
    // Only process valid chat messages
    if (!data.messageId || !data.content || !data.senderName || !data.sentAt) {
      return;
    }

    // Only show messages for current room
    if (data.roomId === this.currentRoomId) {
      const messagesContainer = document.getElementById("messages-container");
      if (!messagesContainer) return;

      // Remove loading state if present
      const loadingState = messagesContainer.querySelector(
        ".text-center.text-muted"
      );
      if (loadingState) {
        loadingState.remove();
      }

      // Add message to UI
      const messageElement = this.createMessageElement(data);
      messagesContainer.appendChild(messageElement);

      // Scroll to bottom
      this.scrollToBottom();
    }

    // Always update room list for unread count updates
    this.updateRoomList();

    // Update last message for the room
    this.updateRoomLastMessage(data.roomId, data.content, data.sentAt);
  }

  // Handle typing indicator
  handleTypingIndicator(data) {
    // Implementation for typing indicators
    console.log("Typing indicator:", data);
  }

  // Update room list (for unread count updates)
  async updateRoomList() {
    try {
      const response = await fetch("/api/chat/available-restaurants");
      if (response.ok) {
        const restaurants = await response.json();
        this.displayRestaurants(restaurants);
      }
    } catch (error) {
      console.error("Error updating room list:", error);
    }
  }

  // Handle typing
  handleTyping() {
    if (!this.currentRoomId || !this.isConnected) return;

    // Clear existing timer
    if (this.typingTimer) {
      clearTimeout(this.typingTimer);
    }

    // Send typing indicator
    this.stompClient.send(
      "/app/chat/typing",
      {},
      JSON.stringify({
        roomId: this.currentRoomId,
        typing: true,
      })
    );

    // Set timer to stop typing indicator
    this.typingTimer = setTimeout(() => {
      this.stompClient.send(
        "/app/chat/typing",
        {},
        JSON.stringify({
          roomId: this.currentRoomId,
          typing: false,
        })
      );
    }, 1000);
  }

  // Scroll to bottom
  scrollToBottom() {
    const messagesContainer = document.getElementById("messages-container");
    if (messagesContainer) {
      messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
  }

  // Show chat interface
  showChatInterface() {
    const welcomeScreen = document.getElementById("welcome-screen");
    const chatInterface = document.getElementById("chat-interface");

    if (welcomeScreen) welcomeScreen.style.display = "none";
    if (chatInterface) chatInterface.style.display = "flex";
  }

  // Show welcome screen
  showWelcomeScreen() {
    const welcomeScreen = document.getElementById("welcome-screen");
    const chatInterface = document.getElementById("chat-interface");

    if (welcomeScreen) welcomeScreen.style.display = "flex";
    if (chatInterface) chatInterface.style.display = "none";

    this.currentRoomId = null;
  }

  // Mark messages as read
  async markMessagesAsRead(roomId) {
    try {
      await fetch(`/api/chat/rooms/${roomId}/read`, { method: "POST" });
    } catch (error) {
      console.error("Failed to mark messages as read:", error);
    }
  }

  // Update room selection
  updateRoomSelection(roomId) {
    // Remove active class from all rooms and AI assistant
    document.querySelectorAll(".chat-room-item, .ai-assistant-room-item").forEach((item) => {
      item.classList.remove("active");
    });

    // Add active class to current room
    const currentRoom = document.querySelector(`[data-room-id="${roomId}"]`);
    if (currentRoom) {
      currentRoom.classList.add("active");
    } else {
      // If room not found, check if it's AI restaurant by checking restaurantId in room data
      // Try to get room info to check restaurantId
      fetch(`/api/chat/rooms/${roomId}`)
        .then(response => response.ok ? response.json() : null)
        .then(roomData => {
          if (roomData && roomData.restaurantId === 37) {
            const aiItem = document.getElementById("ai-assistant-room-item");
            if (aiItem) {
              aiItem.classList.add("active");
            }
          }
        })
        .catch(error => {
          console.error("Error checking room for AI:", error);
        });
    }
  }

  // Update room last message
  updateRoomLastMessage(roomId, content, sentAt) {
    const roomItem = document.querySelector(`[data-room-id="${roomId}"]`);
    if (roomItem) {
      const lastMessageElement = roomItem.querySelector(".room-last-message");
      const timeElement = roomItem.querySelector(".room-time");

      if (lastMessageElement) {
        lastMessageElement.textContent = content;
      }

      if (timeElement) {
        timeElement.textContent = this.formatTime(sentAt);
      }
    }
  }

  // Toggle sidebar on mobile
  toggleSidebar() {
    const sidebar = document.getElementById("chat-sidebar");
    if (sidebar) {
      sidebar.classList.toggle("show");
    }
  }

  // Show error message
  showError(message) {
    const template = document.getElementById("error-template");
    const errorElement = template.content.cloneNode(true);

    const errorMessage = errorElement.querySelector(".error-message");
    const closeButton = errorElement.querySelector(".error-close");

    errorMessage.textContent = message;

    closeButton.addEventListener("click", () => {
      const notification = closeButton.closest(".error-notification");
      if (notification) {
        notification.remove();
      }
    });

    document.body.appendChild(errorElement);

    // Auto remove after 5 seconds
    setTimeout(() => {
      const notification = document.querySelector(".error-notification");
      if (notification) {
        notification.remove();
      }
    }, 5000);
  }
}

// Handle unread count updates
function handleUnreadCountUpdate(data) {
  console.log("Received unread count update:", data);

  // Update restaurant-specific unread count
  const restaurantItem = document.querySelector(
    `[data-room-id="${data.roomId}"]`
  );
  console.log("Found restaurant item:", restaurantItem);

  if (restaurantItem) {
    const unreadElement = restaurantItem.querySelector(".restaurant-unread");
    console.log("Found unread element:", unreadElement);

    if (data.roomUnreadCount > 0) {
      if (unreadElement) {
        unreadElement.textContent = data.roomUnreadCount;
        unreadElement.style.display = "flex";
        console.log("Updated existing unread element:", data.roomUnreadCount);
      } else {
        // Create unread badge if it doesn't exist
        const restaurantMeta = restaurantItem.querySelector(".restaurant-meta");
        console.log("Found restaurant meta:", restaurantMeta);
        if (restaurantMeta) {
          const unreadBadge = document.createElement("span");
          unreadBadge.className = "restaurant-unread";
          unreadBadge.textContent = data.roomUnreadCount;
          restaurantMeta.appendChild(unreadBadge);
          console.log("Created new unread badge:", data.roomUnreadCount);
        }
      }
    } else {
      if (unreadElement) {
        unreadElement.style.display = "none";
        console.log("Hidden unread element");
      }
    }
  } else {
    console.log("No restaurant item found for room:", data.roomId);
  }

  // Update total unread count
  updateTotalUnreadCount(data.totalUnreadCount);

  // Show notification if not in current room
  if (data.roomId !== window.chatManager?.currentRoomId) {
    showNotification(data.roomId, data.roomUnreadCount);
  }
}

// Update total unread count in header
function updateTotalUnreadCount(totalCount) {
  const totalUnreadBadge = document.getElementById("total-unread-badge");
  const totalUnreadCount = document.getElementById("total-unread-count");

  if (totalUnreadBadge && totalUnreadCount) {
    if (totalCount > 0) {
      totalUnreadCount.textContent = totalCount;
      totalUnreadBadge.style.display = "flex";
    } else {
      totalUnreadBadge.style.display = "none";
    }
  }
}

// Show notification for new messages
function showNotification(roomId, unreadCount) {
  // Create notification element
  const notification = document.createElement("div");
  notification.className = "notification-toast";
  notification.innerHTML = `
        <div class="notification-content">
            <i class="fas fa-comment"></i>
            <span>Có ${unreadCount} tin nhắn mới</span>
        </div>
    `;

  // Add to page
  document.body.appendChild(notification);

  // Show notification
  setTimeout(() => {
    notification.classList.add("show");
  }, 100);

  // Hide notification after 3 seconds
  setTimeout(() => {
    notification.classList.remove("show");
    setTimeout(() => {
      document.body.removeChild(notification);
    }, 300);
  }, 3000);
}

// Initialize chat manager when DOM is ready
document.addEventListener("DOMContentLoaded", () => {
  window.chatManager = new CustomerChatManager();
  window.customerChatManager = window.chatManager; // Also expose as customerChatManager for HTML scripts
});
