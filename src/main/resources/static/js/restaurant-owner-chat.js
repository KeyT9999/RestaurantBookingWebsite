/**
 * Restaurant Owner Chat JavaScript
 * Handles WebSocket communication and UI interactions for restaurant owner chat
 */

class RestaurantOwnerChatManager {
  constructor() {
    this.socket = null;
    this.stompClient = null;
    this.currentRoomId = null;
    this.currentParticipantId = null;
    this.typingSubscription = null;
    this.currentUserId = null;
    this.userRole = null;
    this.isConnected = false;
    this.typingTimer = null;
    this.reconnectAttempts = 0;
    this.onlineStatusTimer = null;
    
    // Infinite scroll properties
    this.currentPage = 0;
    this.pageSize = 100;
    this.hasMoreMessages = true;
    this.isLoadingMessages = false;
    this.allMessages = [];

    try {
      this.init();
    } catch (error) {
      console.error("Error in RestaurantOwnerChatManager constructor:", error);
    }
  }

  init() {
    try {
      this.loadUserInfo();
      this.initWebSocket();
      this.setupEventListeners();
      this.setupRoomClickHandlers();
      this.setupInfiniteScroll();
      this.loadAvailableAdmins(); // Load admin list on page load
    } catch (error) {
      console.error("Error in init method:", error);
    }
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

    // Subscribe to typing indicators - need to subscribe to specific room
    // Will be subscribed when opening a room

    // Subscribe to user-specific errors
    this.stompClient.subscribe("/user/queue/errors", (message) => {
      const data = JSON.parse(message.body);
      this.showError(data.message);
    });

    // Subscribe to unread count updates
    this.stompClient.subscribe("/user/queue/unread-updates", (message) => {
      const data = JSON.parse(message.body);
      handleUnreadCountUpdate(data);
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

  // Reset reconnection attempts on successful connection
  resetReconnectAttempts() {
    this.reconnectAttempts = 0;
  }

  // Setup event listeners
  setupEventListeners() {
    // Message input events
    const messageInput = document.getElementById("message-input");
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

    // Send button
    const sendButton = document.getElementById("send-button");
    if (sendButton) {
      sendButton.addEventListener("click", () => {
        this.sendMessage();
      });
    }

    // Back button
    const backButton = document.getElementById("back-to-list");
    if (backButton) {
      backButton.addEventListener("click", () => {
        this.backToRoomList();
      });
    }
  }

  // Setup room click handlers
  setupRoomClickHandlers() {
    const roomItems = document.querySelectorAll(".chat-room-item");
    roomItems.forEach((item) => {
      item.addEventListener("click", () => {
        const roomId = item.getAttribute("data-room-id");
        this.openChatRoom(roomId);
      });
    });
  }

  // Open chat room
  async openChatRoom(roomId) {
    // Unsubscribe from previous room's typing indicator
    if (this.typingSubscription) {
      this.typingSubscription.unsubscribe();
      this.typingSubscription = null;
    }
    
    // Hide online status when switching rooms
    this.hideOnlineStatus();
    
    this.currentRoomId = roomId;

    // Find room data to get participantId
    const roomElement = document.querySelector(`[data-room-id="${roomId}"]`);
    if (roomElement) {
      // Try to get participantId from room data attribute
      const participantId = roomElement.getAttribute("data-participant-id");
      this.currentParticipantId = participantId || null;
    }

    // Update active room
    document.querySelectorAll(".chat-room-item").forEach((item) => {
      item.classList.remove("active");
    });
    const activeRoom = document.querySelector(`[data-room-id="${roomId}"]`);
    if (activeRoom) {
      activeRoom.classList.add("active");
    }

    // Hide welcome screen and show chat interface
    const welcomeScreen = document.getElementById("welcome-screen");
    const chatInterface = document.getElementById("chat-interface");

    if (welcomeScreen) {
      welcomeScreen.style.display = "none";
    } else {
      console.warn("Welcome screen element not found");
    }

    if (chatInterface) {
      chatInterface.style.display = "flex";
    } else {
      console.warn("Chat interface element not found");
    }

    // Reset pagination for new room
    this.currentPage = 0;
    this.hasMoreMessages = true;
    this.allMessages = [];
    
    // Load messages
    await this.loadMessages(roomId);

    // Mark messages as read
    await this.markMessagesAsRead(roomId);

    // Join room via WebSocket
    this.joinRoom(roomId);
    
    // Subscribe to typing indicator for this specific room
    if (this.isConnected && this.stompClient && roomId) {
      this.typingSubscription = this.stompClient.subscribe(
        `/topic/room/${roomId}/typing`,
        (message) => {
          const data = JSON.parse(message.body);
          this.handleTypingIndicator(data, roomId);
        }
      );
    }

    // Update room info
    this.updateRoomInfo(roomId);
  }

  // Load messages for a room
  async loadMessages(roomId) {
    if (this.isLoadingMessages) return;
    
    this.isLoadingMessages = true;
    this.showLoadingState();

    try {
      const response = await fetch(
        `/api/chat/rooms/${roomId}/messages?page=${this.currentPage}&size=${this.pageSize}`
      );
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
        
      }
    } catch (error) {
      console.error("Failed to load messages:", error);
    } finally {
      this.isLoadingMessages = false;
    }
  }

  // Display messages in chat interface
  displayMessages(messages) {
    const container = document.getElementById("messages-container");
    container.innerHTML = "";

    messages.forEach((message) => {
      const messageElement = this.createMessageElement(message);
      container.appendChild(messageElement);
    });

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

  // Load more messages (infinite scroll)
  async loadMoreMessages() {
    if (!this.currentRoomId || this.isLoadingMessages || !this.hasMoreMessages) return;
    
    this.isLoadingMessages = true;
    this.showLoadMoreIndicator();

    try {
      const response = await fetch(
        `/api/chat/rooms/${this.currentRoomId}/messages?page=${this.currentPage}&size=${this.pageSize}`
      );
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

  // Show loading state
  showLoadingState() {
    const messagesContainer = document.getElementById("messages-container");
    if (!messagesContainer) return;

    messagesContainer.innerHTML = `
      <div class="text-center text-muted">
        <div class="spinner-border spinner-border-sm text-gold" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
        <small>Đang tải...</small>
      </div>
    `;
  }

  // Create message element from template
  createMessageElement(message) {
    const template = document.getElementById("message-template");
    const clone = template.content.cloneNode(true);
    const messageItem = clone.querySelector(".message-item");

    messageItem.setAttribute("data-message-id", message.messageId);

    // Safe handling of senderName
    const senderName = message.senderName || "Người dùng";
    messageItem.querySelector(".message-sender").textContent = senderName;

    // Safe handling of sentAt
    const timeText = this.formatTime(message.sentAt);
    messageItem.querySelector(".message-time").textContent = timeText;

    // Safe handling of content - use innerHTML since content is sanitized on server
    const content = message.content || "";
    messageItem.querySelector(".message-text").innerHTML = content;

    // Check if message is from current user
    if (message.senderId === this.currentUserId) {
      messageItem.classList.add("own-message");
    }

    return messageItem;
  }

  // Send message
  async sendMessage() {
    const messageInput = document.getElementById("message-input");
    const content = messageInput.value.trim();

    if (!content || !this.currentRoomId || !this.isConnected) {
      this.showError(
        "Không thể gửi tin nhắn: " +
          (!content
            ? "Nội dung trống"
            : !this.currentRoomId
            ? "Chưa chọn phòng chat"
            : "Chưa kết nối WebSocket")
      );
      return;
    }

    // Validate message length
    if (content.length > 1000) {
      this.showError("Tin nhắn quá dài (tối đa 1000 ký tự)");
      return;
    }

    try {
      this.stompClient.send(
        "/app/chat.sendMessage",
        {},
        JSON.stringify({
          roomId: this.currentRoomId,
          content: content,
        })
      );

      messageInput.value = "";
      this.stopTyping();
    } catch (error) {
      console.error("Failed to send message:", error);
      this.showError("Không thể gửi tin nhắn. Vui lòng thử lại.");
    }
  }

  // Handle incoming message
  handleIncomingMessage(data) {
    // Only process valid chat messages (not join notifications or other events)
    if (
      !data ||
      !data.messageId ||
      !data.content ||
      !data.senderName ||
      !data.sentAt
    ) {
      console.log("Ignoring non-message data:", data);
      return;
    }

    if (data.roomId === this.currentRoomId) {
      // Add message to current chat
      const messageElement = this.createMessageElement(data);
      document.getElementById("messages-container").appendChild(messageElement);
      this.scrollToBottom();
      
      // Show online status when receiving message from participant (not from self)
      if (data.senderId !== this.currentUserId && 
          data.senderId !== this.currentUserId?.toString() &&
          (this.currentParticipantId === null || 
           data.senderId === this.currentParticipantId || 
           data.senderId === this.currentParticipantId?.toString())) {
        this.showOnlineStatus();
      }
    }

    // Check if message belongs to current restaurant before updating room list
    const currentRestaurantId = this.getCurrentRestaurantId();
    
    // Find room element to check its restaurantId
    const roomElement = document.querySelector(`[data-room-id="${data.roomId}"]`);
    let roomRestaurantId = null;
    
    if (roomElement) {
      // Get restaurantId from room element
      roomRestaurantId = roomElement.getAttribute("data-restaurant-id");
    }
    
    // If we have restaurantId in message data, use it as fallback
    if (!roomRestaurantId && data.restaurantId) {
      roomRestaurantId = data.restaurantId.toString();
    }
    
    // Only update room list if message is from current restaurant
    if (currentRestaurantId && roomRestaurantId) {
      // Compare restaurant IDs
      const currentRestaurantIdStr = currentRestaurantId.toString();
      const messageRestaurantIdStr = roomRestaurantId.toString();
      
      if (currentRestaurantIdStr === messageRestaurantIdStr) {
        // Message is from current restaurant, update room list
        this.updateRoomList();
      }
      // Otherwise, ignore - message is for another restaurant
    } else if (!currentRestaurantId) {
      // No restaurant selected, update anyway (for backward compatibility)
      this.updateRoomList();
    } else if (data.roomId === this.currentRoomId) {
      // If it's the current room but restaurantId not found, update for safety
      this.updateRoomList();
    }
    // Otherwise, ignore - message is for another restaurant
  }

  // Handle typing indicator
  handleTypingIndicator(data, roomId) {
    // Only process typing indicator for current room
    if (roomId && roomId !== this.currentRoomId) {
      return;
    }
    
    // Don't show typing indicator for own messages
    if (data.userId === this.currentUserId || data.userId === this.currentUserId?.toString()) {
      return;
    }

    const typingIndicator = document.getElementById("typing-indicator");
    if (typingIndicator) {
      if (data.typing) {
        typingIndicator.style.display = "flex";
        // Show online status when participant is typing
        this.showOnlineStatus();
      } else {
        typingIndicator.style.display = "none";
      }
    }
  }

  // Show online status
  showOnlineStatus() {
    const chatStatus = document.getElementById("chat-status");
    if (chatStatus) {
      chatStatus.style.display = "flex";
    }
    
    // Hide online status after 30 seconds of inactivity
    if (this.onlineStatusTimer) {
      clearTimeout(this.onlineStatusTimer);
    }
    
    this.onlineStatusTimer = setTimeout(() => {
      this.hideOnlineStatus();
    }, 30000); // 30 seconds
  }

  // Hide online status
  hideOnlineStatus() {
    const chatStatus = document.getElementById("chat-status");
    if (chatStatus) {
      chatStatus.style.display = "none";
    }
    
    if (this.onlineStatusTimer) {
      clearTimeout(this.onlineStatusTimer);
      this.onlineStatusTimer = null;
    }
  }

  // Handle typing
  handleTyping() {
    if (!this.currentRoomId || !this.isConnected) return;

    // Send typing indicator
    this.stompClient.send(
      "/app/chat.typing",
      {},
      JSON.stringify({
        roomId: this.currentRoomId,
        typing: true,
      })
    );

    // Clear previous timer
    if (this.typingTimer) {
      clearTimeout(this.typingTimer);
    }

    // Set timer to stop typing indicator
    this.typingTimer = setTimeout(() => {
      this.stopTyping();
    }, 1000);
  }

  // Stop typing indicator
  stopTyping() {
    if (!this.currentRoomId || !this.isConnected) return;

    this.stompClient.send(
      "/app/chat.typing",
      {},
      JSON.stringify({
        roomId: this.currentRoomId,
        typing: false,
      })
    );

    if (this.typingTimer) {
      clearTimeout(this.typingTimer);
      this.typingTimer = null;
    }
  }

  // Join room
  joinRoom(roomId) {
    if (!this.isConnected) return;

    this.stompClient.send(
      "/app/chat.joinRoom",
      {},
      JSON.stringify({
        roomId: roomId,
      })
    );
  }

  // Mark messages as read
  async markMessagesAsRead(roomId) {
    try {
      await fetch(`/api/chat/rooms/${roomId}/read`, { method: "POST" });
    } catch (error) {
      console.error("Failed to mark messages as read:", error);
    }
  }

  // Update room list
  async updateRoomList() {
    try {
      // Get current restaurantId from URL or hidden input
      const restaurantId = this.getCurrentRestaurantId();
      
      // Build URL with restaurantId if available
      let url = "/api/chat/rooms";
      if (restaurantId) {
        url += `?restaurantId=${restaurantId}`;
      }
      
      const response = await fetch(url);
      if (response.ok) {
        const rooms = await response.json();
        this.updateRoomsDisplay(rooms);
        this.updateTotalUnreadBadge(rooms);
      }
    } catch (error) {
      console.error("Failed to update room list:", error);
    }
  }

  // Update rooms display
  updateRoomsDisplay(rooms) {
    const roomsList = document.getElementById("chat-rooms-list");
    roomsList.innerHTML = "";

    if (rooms.length === 0) {
      roomsList.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-comments text-muted"></i>
                    <p class="text-muted">Chưa có cuộc trò chuyện nào</p>
                </div>
            `;
      return;
    }

    rooms.forEach((room) => {
      const roomElement = this.createRoomElement(room);
      roomsList.appendChild(roomElement);
    });

    // Re-setup click handlers
    this.setupRoomClickHandlers();
  }

  // Create room element
  createRoomElement(room) {
    const roomItem = document.createElement("div");
    roomItem.className = "chat-room-item";
    roomItem.setAttribute("data-room-id", room.roomId);
    if (room.participantId) {
      roomItem.setAttribute("data-participant-id", room.participantId);
    }
    if (room.restaurantId) {
      roomItem.setAttribute("data-restaurant-id", room.restaurantId);
    }

    const iconClass =
      room.participantRole === "CUSTOMER" ? "fa-user" : "fa-user-shield";
    
    // Avatar HTML - show image if available, otherwise show icon
    const avatarHtml = room.participantAvatarUrl
      ? `<img src="${room.participantAvatarUrl}" alt="${room.participantName || 'Avatar'}" class="avatar-image" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
         <div class="avatar-fallback" style="display:none;">
             <i class="fas ${iconClass}"></i>
         </div>`
      : `<div class="avatar-fallback">
             <i class="fas ${iconClass}"></i>
         </div>`;

    roomItem.innerHTML = `
            <div class="room-avatar">
                ${avatarHtml}
            </div>
            <div class="room-info">
                <div class="room-name">
                    <span>${room.participantName}</span>
                    <span class="role-badge ${
                      room.participantRole === "ADMIN"
                        ? "role-admin"
                        : "role-customer"
                    }">
                        ${
                          room.participantRole === "ADMIN"
                            ? "Admin"
                            : "Khách hàng"
                        }
                    </span>
                </div>
                <div class="room-last-message">${
                  room.lastMessage || "Chưa có tin nhắn"
                }</div>
                <div class="room-meta">
                    <span class="room-time">${this.formatTime(
                      room.lastMessageAt
                    )}</span>
                    ${
                      room.unreadCount > 0
                        ? `<span class="room-unread">${room.unreadCount}</span>`
                        : ""
                    }
                </div>
            </div>
        `;

    return roomItem;
  }

  // Update total unread badge
  updateTotalUnreadBadge(rooms) {
    const totalUnread = rooms.reduce(
      (sum, room) => sum + (room.unreadCount || 0),
      0
    );
    const badge = document.getElementById("total-unread-badge");

    if (totalUnread > 0) {
      const totalUnreadCount = document.getElementById("total-unread-count");
      if (totalUnreadCount) totalUnreadCount.textContent = totalUnread;
      if (badge) badge.style.display = "flex";
    } else {
      if (badge) badge.style.display = "none";
    }
  }

  // Update room info
  updateRoomInfo(roomId) {
    // Find the room data from the current rooms list
    const roomElement = document.querySelector(`[data-room-id="${roomId}"]`);
    if (roomElement) {
      // Lấy tên participant từ span đầu tiên trong room-name
      const participantNameSpan = roomElement.querySelector(
        ".room-name span:first-child"
      );
      const participantName = participantNameSpan
        ? participantNameSpan.textContent
        : "Participant";
      
      // Update currentParticipantId from room element
      const participantId = roomElement.getAttribute("data-participant-id");
      this.currentParticipantId = participantId || null;

      const participantNameElement =
        document.getElementById("participant-name");

      if (participantNameElement)
        participantNameElement.textContent = participantName;
    } else {
      // Fallback if room element not found
      const participantNameElement =
        document.getElementById("participant-name");

      if (participantNameElement)
        participantNameElement.textContent = "Participant";
      
      this.currentParticipantId = null;
    }
  }

  // Back to room list
  backToRoomList() {
    const welcomeScreen = document.getElementById("welcome-screen");
    const chatInterface = document.getElementById("chat-interface");

    if (welcomeScreen) welcomeScreen.style.display = "flex";
    if (chatInterface) chatInterface.style.display = "none";

    // Remove active class from all rooms
    document.querySelectorAll(".chat-room-item").forEach((item) => {
      item.classList.remove("active");
    });

    // Unsubscribe from typing indicator
    if (this.typingSubscription) {
      this.typingSubscription.unsubscribe();
      this.typingSubscription = null;
    }
    
    // Hide online status when leaving room
    this.hideOnlineStatus();

    this.currentRoomId = null;
    this.currentParticipantId = null;
  }

  // Scroll to bottom of messages
  scrollToBottom() {
    const container = document.getElementById("messages-container");
    container.scrollTop = container.scrollHeight;
  }

  // Format time for display
  formatTime(dateTimeString) {
    // Handle invalid or null dateTimeString
    if (!dateTimeString) {
      return "Thời gian không xác định";
    }

    const date = new Date(dateTimeString);

    // Check if date is valid
    if (isNaN(date.getTime())) {
      return "Thời gian không hợp lệ";
    }

    const now = new Date();
    const diff = now - date;

    if (diff < 60000) {
      // Less than 1 minute
      return "Vừa xong";
    } else if (diff < 3600000) {
      // Less than 1 hour
      return Math.floor(diff / 60000) + " phút trước";
    } else if (diff < 86400000) {
      // Less than 1 day
      return Math.floor(diff / 3600000) + " giờ trước";
    } else {
      return date.toLocaleDateString("vi-VN");
    }
  }

  // Show error message
  showError(message) {
    console.error("Chat Error:", message);

    // Create error notification
    const errorDiv = document.createElement("div");
    errorDiv.className = "error-notification";
    errorDiv.innerHTML = `
            <div class="error-content">
                <i class="fas fa-exclamation-triangle"></i>
                <span class="error-message">${message}</span>
                <button class="error-close">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `;

    // Insert at the top of chat interface
    const chatInterface = document.getElementById("chat-interface");
    if (chatInterface) {
      chatInterface.insertBefore(errorDiv, chatInterface.firstChild);
    }

    // Auto-hide after 5 seconds
    setTimeout(() => {
      if (errorDiv && errorDiv.parentNode) {
        errorDiv.remove();
      }
    }, 5000);

    // Close button functionality
    errorDiv.querySelector(".error-close").addEventListener("click", () => {
      errorDiv.remove();
    });
  }

  // Chat with Admin functionality
  async loadAvailableAdmins() {
    try {
      console.log("Loading available admins...");
      const response = await fetch("/api/chat/available-admins");
      console.log("Response status:", response.status);

      if (response.ok) {
        const admins = await response.json();
        console.log("Admins loaded:", admins);
        this.displayAdmins(admins);
      } else if (response.status === 401) {
        console.error("Authentication required");
        this.showError("Vui lòng đăng nhập để sử dụng tính năng chat");
        // Redirect to login page
        window.location.href = "/login";
      } else if (response.status === 403) {
        console.error("Access denied");
        this.showError("Chỉ restaurant owner mới có thể chat với admin");
      } else {
        const errorText = await response.text();
        console.error("Error response:", errorText);
        this.showError("Không thể tải danh sách admin: " + errorText);
      }
    } catch (error) {
      console.error("Error loading admins:", error);
      this.showError("Không thể tải danh sách admin");
    }
  }

  displayAdmins(admins) {
    console.log("Displaying admins:", admins);
    const adminList = document.getElementById("admin-list");
    if (!adminList) {
      console.error("Admin list element not found");
      return;
    }

    if (admins.length === 0) {
      console.log("No admins found");
      adminList.innerHTML = `
                <div class="text-center text-muted">
                    <i class="fas fa-user-shield fa-2x mb-2"></i>
                    <p class="mb-0">Không có admin nào</p>
                </div>
            `;
      return;
    }

    adminList.innerHTML = "";
    admins.forEach((admin) => {
      console.log("Creating admin item for:", admin);
      const adminItem = this.createAdminItem(admin);
      adminList.appendChild(adminItem);
    });
  }

  createAdminItem(admin) {
    console.log("Creating admin item:", admin);
    const template = document.getElementById("admin-item-template");
    if (!template) {
      console.error("Admin item template not found");
      return null;
    }

    const adminItem = template.content.cloneNode(true);

    const adminDiv = adminItem.querySelector(".admin-item");
    adminDiv.setAttribute("data-admin-id", admin.adminId);

    adminDiv.querySelector(".admin-name").textContent = admin.adminName;
    adminDiv.querySelector(".admin-email").textContent = admin.adminEmail;

    // Add click handler for chat button - automatically use current restaurant
    adminDiv.querySelector(".start-chat-btn").addEventListener("click", () => {
      const restaurantId = this.getCurrentRestaurantId();
      if (restaurantId) {
        this.startChatWithAdmin(admin.adminId, restaurantId);
      } else {
        this.showError("Vui lòng chọn nhà hàng ở header trước khi chat với admin");
      }
    });

    return adminDiv;
  }

  // Get current restaurant ID from URL parameter or hidden input
  getCurrentRestaurantId() {
    // Try to get from URL parameter first
    const urlParams = new URLSearchParams(window.location.search);
    const restaurantIdFromUrl = urlParams.get('restaurantId');
    
    if (restaurantIdFromUrl) {
      return restaurantIdFromUrl;
    }
    
    // Try to get from hidden input
    const hiddenInput = document.getElementById('current-restaurant-id');
    if (hiddenInput && hiddenInput.value) {
      return hiddenInput.value;
    }
    
    // Try to get from restaurant dropdown in header
    const restaurantDropdown = document.querySelector('.restaurant-dropdown .restaurant-details');
    if (restaurantDropdown) {
      // Try to find restaurantId from data attribute or other sources
      const restaurantItems = document.querySelectorAll('.restaurant-item.active');
      if (restaurantItems.length > 0) {
        const activeItem = restaurantItems[0];
        const onclickAttr = activeItem.getAttribute('onclick');
        if (onclickAttr) {
          const match = onclickAttr.match(/selectRestaurant\((\d+)\)/);
          if (match && match[1]) {
            return match[1];
          }
        }
      }
    }
    
    return null;
  }

  async startChatWithAdmin(adminId, restaurantId) {
    try {
      console.log(
        "Starting chat with admin:",
        adminId,
        "restaurant:",
        restaurantId
      );

      const response = await fetch("/api/chat/rooms/admin", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `adminId=${adminId}&restaurantId=${restaurantId}`,
      });

      console.log("Response status:", response.status);

      if (response.ok) {
        const result = await response.json();
        console.log("Chat room created:", result);

        // Open the chat room
        this.openChatRoom(result.roomId);

        // Note: Chat rooms list will be refreshed when user navigates back to room list
      } else if (response.status === 401) {
        console.error("Authentication required");
        this.showError("Vui lòng đăng nhập để sử dụng tính năng chat");
        window.location.href = "/login";
      } else if (response.status === 403) {
        console.error("Access denied");
        this.showError("Chỉ restaurant owner mới có thể chat với admin");
      } else {
        const error = await response.text();
        console.error("Error response:", error);
        this.showError("Không thể tạo chat room: " + error);
      }
    } catch (error) {
      console.error("Error starting chat with admin:", error);
      this.showError("Không thể bắt đầu chat với admin");
    }
  }
}

// Global functions
let chatManager;

function openChatRoom(roomId) {
  if (chatManager) {
    chatManager.openChatRoom(roomId);
  }
}

// Handle unread count updates
function handleUnreadCountUpdate(data) {
  console.log("Received unread count update:", data);

  // Update room-specific unread count
  const roomItem = document.querySelector(`[data-room-id="${data.roomId}"]`);
  if (roomItem) {
    const unreadElement = roomItem.querySelector(".room-unread");
    if (data.roomUnreadCount > 0) {
      if (unreadElement) {
        unreadElement.textContent = data.roomUnreadCount;
        unreadElement.style.display = "flex";
      } else {
        // Create unread badge if it doesn't exist
        const roomMeta = roomItem.querySelector(".room-meta");
        if (roomMeta) {
          const unreadBadge = document.createElement("span");
          unreadBadge.className = "room-unread";
          unreadBadge.textContent = data.roomUnreadCount;
          roomMeta.appendChild(unreadBadge);
        }
      }
    } else {
      if (unreadElement) {
        unreadElement.style.display = "none";
      }
    }
  }

  // Update total unread count
  updateTotalUnreadCount(data.totalUnreadCount);

  // Show notification if not in current room
  if (data.roomId !== chatManager.currentRoomId) {
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

// Initialize chat manager when page loads
document.addEventListener("DOMContentLoaded", () => {
  try {
    chatManager = new RestaurantOwnerChatManager();

    // Make it globally accessible
    window.chatManager = chatManager;
  } catch (error) {
    console.error("Error initializing chat manager:", error);
  }
});
