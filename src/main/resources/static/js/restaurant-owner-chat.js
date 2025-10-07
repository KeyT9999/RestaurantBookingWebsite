/**
 * Restaurant Owner Chat JavaScript
 * Handles WebSocket communication and UI interactions for restaurant owner chat
 */

class RestaurantOwnerChatManager {
  constructor() {
    this.socket = null;
    this.stompClient = null;
    this.currentRoomId = null;
    this.currentUserId = null;
    this.userRole = null;
    this.isConnected = false;
    this.typingTimer = null;
    this.reconnectAttempts = 0;

    this.init();
  }

  init() {
    this.loadUserInfo();
    this.initWebSocket();
    this.setupEventListeners();
    this.setupRoomClickHandlers();
    this.loadAvailableAdmins(); // Load admin list on page load
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
    this.currentRoomId = roomId;

    // Update active room
    document.querySelectorAll(".chat-room-item").forEach((item) => {
      item.classList.remove("active");
    });
    document
      .querySelector(`[data-room-id="${roomId}"]`)
      .classList.add("active");

    // Hide welcome screen and show chat interface
    const welcomeScreen = document.getElementById("welcome-screen");
    const chatInterface = document.getElementById("chat-interface");

    if (welcomeScreen) welcomeScreen.style.display = "none";
    if (chatInterface) chatInterface.style.display = "flex";

    // Load messages
    await this.loadMessages(roomId);

    // Mark messages as read
    await this.markMessagesAsRead(roomId);

    // Join room via WebSocket
    this.joinRoom(roomId);

    // Update room info
    this.updateRoomInfo(roomId);
  }

  // Load messages for a room
  async loadMessages(roomId) {
    try {
      const response = await fetch(
        `/api/chat/rooms/${roomId}/messages?page=0&size=50`
      );
      if (response.ok) {
        const messages = await response.json();
        this.displayMessages(messages);
      }
    } catch (error) {
      console.error("Failed to load messages:", error);
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

    // Safe handling of content
    const content = message.content || "";
    messageItem.querySelector(".message-text").textContent = content;

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
    }

    // Update rooms list
    this.updateRoomList();
  }

  // Handle typing indicator
  handleTypingIndicator(data) {
    if (data.userId === this.currentUserId) return;

    const typingIndicator = document.getElementById("typing-indicator");
    if (data.typing) {
      typingIndicator.style.display = "flex";
    } else {
      typingIndicator.style.display = "none";
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
      const response = await fetch("/api/chat/rooms");
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

    const iconClass =
      room.participantRole === "CUSTOMER" ? "fa-user" : "fa-user-shield";

    roomItem.innerHTML = `
            <div class="room-avatar">
                <i class="fas ${iconClass}"></i>
            </div>
            <div class="room-info">
                <div class="room-name">${room.participantName}</div>
                <div class="room-restaurant">${room.restaurantName}</div>
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
      document.getElementById("total-unread-count").textContent = totalUnread;
      badge.style.display = "flex";
    } else {
      badge.style.display = "none";
    }
  }

  // Update room info
  updateRoomInfo(roomId) {
    // Find the room data from the current rooms list
    const roomElement = document.querySelector(`[data-room-id="${roomId}"]`);
    if (roomElement) {
      const participantName =
        roomElement.querySelector(".room-name").textContent;
      const restaurantName =
        roomElement.querySelector(".room-restaurant").textContent;

      document.getElementById("participant-name").textContent = participantName;
      document.getElementById("restaurant-name").textContent = restaurantName;
    } else {
      // Fallback if room element not found
      document.getElementById("participant-name").textContent = "Participant";
      document.getElementById("restaurant-name").textContent = "Restaurant";
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

    this.currentRoomId = null;
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
      const response = await fetch("/api/chat/available-admins");
      if (response.ok) {
        const admins = await response.json();
        this.displayAdmins(admins);
      } else {
        throw new Error("Failed to load admins");
      }
    } catch (error) {
      console.error("Error loading admins:", error);
      this.showError("Không thể tải danh sách admin");
    }
  }

  displayAdmins(admins) {
    const adminList = document.getElementById("admin-list");
    if (!adminList) return;

    if (admins.length === 0) {
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
      const adminItem = this.createAdminItem(admin);
      adminList.appendChild(adminItem);
    });
  }

  createAdminItem(admin) {
    const template = document.getElementById("admin-item-template");
    const adminItem = template.content.cloneNode(true);

    const adminDiv = adminItem.querySelector(".admin-item");
    adminDiv.setAttribute("data-admin-id", admin.adminId);

    adminDiv.querySelector(".admin-name").textContent = admin.adminName;
    adminDiv.querySelector(".admin-email").textContent = admin.adminEmail;

    // Add click handler for chat button
    adminDiv.querySelector(".start-chat-btn").addEventListener("click", () => {
      this.startChatWithAdmin(admin.adminId);
    });

    return adminDiv;
  }

  async startChatWithAdmin(adminId) {
    try {
      const response = await fetch("/api/chat/rooms/admin", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `adminId=${adminId}`,
      });

      if (response.ok) {
        const result = await response.json();

        // Open the chat room
        this.openChatRoom(result.roomId);

        // Note: Chat rooms list will be refreshed when user navigates back to room list
      } else {
        const error = await response.text();
        throw new Error(error);
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
document.addEventListener('DOMContentLoaded', () => {
    chatManager = new RestaurantOwnerChatManager();
    
    // Make it globally accessible
    window.chatManager = chatManager;
});
