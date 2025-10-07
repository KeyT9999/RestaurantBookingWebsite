class AdminChatManager {
  constructor() {
    this.adminId = null;
    this.currentRoomId = null;
    this.stompClient = null;
    this.socket = null;
    this.isConnected = false;

    this.init();
  }

  init() {
    this.loadAdminInfo();
    this.initWebSocket();
    this.setupEventListeners();
    this.loadAvailableRestaurants();
    this.loadChatRooms(); // Load rooms with unread count
  }

  loadAdminInfo() {
    // Try to get admin info from page data first
    const adminContainer = document.querySelector(".admin-chat-container");
    if (adminContainer && adminContainer.dataset.adminId) {
      this.adminId = adminContainer.dataset.adminId;
      console.log("Admin ID from template:", this.adminId);
    } else {
      // Fallback: load from API
      this.loadAdminFromAPI();
    }
  }

  async loadAdminFromAPI() {
    try {
      const response = await fetch("/api/user/current");
      if (response.ok) {
        const user = await response.json();
        this.adminId = user.id;
        console.log("Admin ID from API:", this.adminId);
      }
    } catch (error) {
      console.error("Failed to load admin info:", error);
    }
  }

  initWebSocket() {
    try {
      console.log("Initializing WebSocket connection...");
      this.socket = new SockJS("/ws");
      this.stompClient = Stomp.over(this.socket);

      this.stompClient.debug = (str) => {
        console.log("STOMP Debug:", str);
      };

      this.stompClient.connect(
        {},
        (frame) => {
          console.log("✅ Connected to WebSocket:", frame);
          this.isConnected = true;
          this.subscribeToMessages();
        },
        (error) => {
          console.error("❌ WebSocket connection error:", error);
          this.showError("Lỗi kết nối WebSocket: " + error);
        }
      );
    } catch (error) {
      console.error("❌ Error initializing WebSocket:", error);
      this.showError("Không thể khởi tạo WebSocket: " + error);
    }
  }

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
      console.log("Admin received unread update:", data);
      handleUnreadCountUpdate(data);
    });

    // Subscribe to user-specific errors
    this.stompClient.subscribe("/user/queue/errors", (message) => {
      const data = JSON.parse(message.body);
      this.showError(data.message);
    });
  }

  subscribeToRoom(roomId) {
    if (this.stompClient && this.isConnected) {
      // Subscribe to specific room messages
      const roomTopic = `/topic/room/${roomId}`;
      console.log("Subscribing to room topic:", roomTopic);

      this.stompClient.subscribe(roomTopic, (message) => {
        try {
          const data = JSON.parse(message.body);
          console.log("Received room message:", data);
          this.handleIncomingMessage(data);
        } catch (error) {
          console.error("Error parsing room message:", error);
        }
      });
    }
  }

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
      this.addMessageToChat(data);

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

  setupEventListeners() {
    // Send message button
    const sendButton = document.getElementById("send-button");
    if (sendButton) {
      sendButton.addEventListener("click", () => this.sendMessage());
    }

    // Enter key in message input
    const messageInput = document.getElementById("message-input");
    if (messageInput) {
      messageInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
          this.sendMessage();
        }
      });
    }

    // Back button
    const backButton = document.getElementById("back-to-list");
    if (backButton) {
      backButton.addEventListener("click", () => {
        this.showWelcomeScreen();
      });
    }
  }

  // Show welcome screen
  showWelcomeScreen() {
    const chatWelcome = document.getElementById("chat-welcome");
    const chatMessages = document.getElementById("chat-messages");
    const chatInput = document.getElementById("chat-input");

    if (chatWelcome) chatWelcome.style.display = "flex";
    if (chatMessages) chatMessages.style.display = "none";
    if (chatInput) chatInput.style.display = "none";

    // Remove active class from all restaurant items
    document.querySelectorAll(".restaurant-item").forEach((item) => {
      item.classList.remove("active");
    });

    this.currentRoomId = null;
  }

  async loadChatRooms() {
    try {
      console.log("Loading chat rooms with unread count...");
      const response = await fetch("/api/chat/rooms");
      if (response.ok) {
        const rooms = await response.json();
        console.log("Chat rooms loaded:", rooms);
        this.displayRooms(rooms);
        this.updateTotalUnreadBadge(rooms);
      } else {
        console.error("Failed to load chat rooms");
      }
    } catch (error) {
      console.error("Error loading chat rooms:", error);
    }
  }

  // Update total unread badge
  updateTotalUnreadBadge(rooms) {
    const totalUnread = rooms.reduce(
      (sum, room) => sum + (room.unreadCount || 0),
      0
    );
    const badge = document.getElementById("total-unread-badge");
    const count = document.getElementById("total-unread-count");

    if (totalUnread > 0) {
      if (count) count.textContent = totalUnread;
      if (badge) badge.style.display = "flex";
    } else {
      if (badge) badge.style.display = "none";
    }
  }

  async loadAvailableRestaurants() {
    try {
      console.log("Loading available restaurants...");
      const response = await fetch("/api/chat/available-restaurants");
      console.log(
        "Restaurants API response:",
        response.status,
        response.statusText
      );

      if (response.ok) {
        const restaurants = await response.json();
        console.log("Restaurants loaded:", restaurants);
        this.displayRestaurants(restaurants);
      } else {
        const errorText = await response.text();
        console.error("Failed to load restaurants:", errorText);
        throw new Error("Failed to load restaurants: " + errorText);
      }
    } catch (error) {
      console.error("Error loading restaurants:", error);
      this.showError("Không thể tải danh sách nhà hàng: " + error.message);
    }
  }

  displayRestaurants(restaurants) {
    const restaurantList = document.getElementById("restaurant-list");
    if (!restaurantList) return;

    if (restaurants.length === 0) {
      restaurantList.innerHTML = `
                <div class="text-center text-muted">
                    <i class="fas fa-store fa-2x mb-2"></i>
                    <p class="mb-0">Không có nhà hàng nào</p>
                </div>
            `;
      return;
    }

    // Only update if restaurant list is empty (first load)
    if (
      restaurantList.children.length === 1 &&
      restaurantList.querySelector(".spinner-border")
    ) {
      restaurantList.innerHTML = "";
      restaurants.forEach((restaurant) => {
        const restaurantItem = this.createRestaurantItem(restaurant);
        restaurantList.appendChild(restaurantItem);
      });
    }
  }

  createRestaurantItem(restaurant) {
    const template = document.getElementById("restaurant-item-template");
    const restaurantItem = template.content.cloneNode(true);

    const restaurantDiv = restaurantItem.querySelector(".restaurant-item");
    restaurantDiv.setAttribute("data-restaurant-id", restaurant.restaurantId);

    restaurantDiv.querySelector(".restaurant-name").textContent =
      restaurant.restaurantName;
    restaurantDiv.querySelector(".restaurant-owner").textContent =
      restaurant.ownerName;

    restaurantDiv
      .querySelector(".start-chat-btn")
      .addEventListener("click", () => {
        this.startChatWithRestaurant(restaurant.restaurantId);
      });

    return restaurantDiv;
  }

  async startChatWithRestaurant(restaurantId) {
    try {
      console.log("Starting chat with restaurant:", restaurantId);
      const response = await fetch("/api/chat/rooms/restaurant", {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: `restaurantId=${restaurantId}`,
      });

      console.log(
        "Create room API response:",
        response.status,
        response.statusText
      );

      if (response.ok) {
        const result = await response.json();
        console.log("Room created:", result);

        // Update restaurant item with room ID
        const restaurantItem = document.querySelector(
          `[data-restaurant-id="${restaurantId}"]`
        );
        if (restaurantItem) {
          restaurantItem.dataset.roomId = result.roomId;
        }

        this.openChatRoom(result.roomId);
      } else {
        const error = await response.text();
        console.error("Failed to create room:", error);
        throw new Error(error);
      }
    } catch (error) {
      console.error("Error starting chat with restaurant:", error);
      this.showError("Không thể bắt đầu chat với nhà hàng: " + error.message);
    }
  }

  async openChatRoom(roomId) {
    console.log("Opening chat room:", roomId);
    this.currentRoomId = roomId;

    // Show loading state
    this.showLoadingState();

    // Update restaurant info in header
    this.updateRestaurantInfo(roomId);

    // Join room via WebSocket
    if (this.isConnected) {
      console.log("🔗 Joining room via WebSocket:", roomId);
      this.stompClient.send(
        "/app/chat/joinRoom",
        {},
        JSON.stringify({
          roomId: roomId,
        })
      );
    } else {
      console.warn("⚠️ WebSocket not connected, cannot join room");
    }

    // Load room messages
    const response = await fetch(`/api/chat/rooms/${roomId}/messages`);
    if (response.ok) {
      const messages = await response.json();
      this.displayMessages(messages);
      this.showChatInterface();
      this.updateRoomSelection(roomId);

      // Mark messages as read after loading
      await this.markMessagesAsRead(roomId);
    } else {
      this.showError("Không thể tải tin nhắn");
    }
  }

  // Update restaurant info in chat header
  updateRestaurantInfo(roomId) {
    // Find restaurant item with this room ID
    const restaurantItem = document.querySelector(`[data-room-id="${roomId}"]`);
    if (restaurantItem) {
      const restaurantName = restaurantItem.querySelector(".restaurant-name");
      const restaurantOwner = restaurantItem.querySelector(".restaurant-owner");

      if (restaurantName) {
        const restaurantNameElement =
          document.getElementById("restaurant-name");
        if (restaurantNameElement) {
          restaurantNameElement.textContent = restaurantName.textContent;
        }
      }

      console.log("Updated restaurant info:", restaurantName?.textContent);
    } else {
      console.log("No restaurant item found for room:", roomId);
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

  // Show chat interface
  showChatInterface() {
    const chatWelcome = document.getElementById("chat-welcome");
    const chatMessages = document.getElementById("chat-messages");
    const chatInput = document.getElementById("chat-input");

    if (chatWelcome) chatWelcome.style.display = "none";
    if (chatMessages) chatMessages.style.display = "flex";
    if (chatInput) chatInput.style.display = "block";
  }

  // Update room selection
  updateRoomSelection(roomId) {
    // Remove active class from all restaurant items
    document.querySelectorAll(".restaurant-item").forEach((item) => {
      item.classList.remove("active");
    });

    // Add active class to current room's restaurant item
    const currentRestaurantItem = document.querySelector(
      `[data-room-id="${roomId}"]`
    );
    if (currentRestaurantItem) {
      currentRestaurantItem.classList.add("active");
    }
  }

  // Mark messages as read
  async markMessagesAsRead(roomId) {
    try {
      await fetch(`/api/chat/rooms/${roomId}/read`, { method: "POST" });
    } catch (error) {
      console.error("Failed to mark messages as read:", error);
    }
  }

  async loadMessages(roomId) {
    try {
      const response = await fetch(
        `/api/chat/rooms/${roomId}/messages?page=0&size=50`
      );
      if (response.ok) {
        const messages = await response.json();
        this.displayMessages(messages);
      } else {
        throw new Error("Failed to load messages");
      }
    } catch (error) {
      console.error("Error loading messages:", error);
      this.showError("Không thể tải tin nhắn");
    }
  }

  displayMessages(messages) {
    const messagesContainer = document.getElementById("messages-container");
    if (!messagesContainer) return;

    messagesContainer.innerHTML = "";
    messages.forEach((message) => {
      this.addMessageToChat(message);
    });

    // Scroll to bottom
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  }

  addMessageToChat(message) {
    const messagesContainer = document.getElementById("messages-container");
    if (!messagesContainer) return;

    const template = document.getElementById("message-template");
    const messageItem = template.content.cloneNode(true);

    // Get the actual DOM element from the DocumentFragment
    const messageElement = messageItem.querySelector(".message-item");
    if (!messageElement) {
      console.error("Message template does not contain .message-item element");
      console.log("Available elements in template:", messageItem.children);
      return;
    }

    console.log("Found messageElement:", messageElement);
    console.log(
      "messageElement.classList before:",
      messageElement.classList.toString()
    );

    messageElement.querySelector(".sender-name").textContent =
      message.senderName;
    messageElement.querySelector(".message-text").textContent = message.content;
    messageElement.querySelector(".message-time").textContent = this.formatTime(
      message.sentAt
    );

    // Debug logging
    console.log("=== DEBUG ADMIN MESSAGE ===");
    console.log("Message:", message);
    console.log("Admin ID:", this.adminId);
    console.log("Sender ID:", message.senderId);
    console.log("Sender Role:", message.senderRole);

    // Add role-based classes for better styling
    try {
      console.log(
        "Comparing senderId:",
        message.senderId,
        "with adminId:",
        this.adminId
      );
      console.log("Are they equal?", message.senderId === this.adminId);

      if (message.senderId === this.adminId) {
        // Message from current admin
        console.log("Adding admin-message class");
        messageElement.classList.add("own-message", "admin-message");
        console.log(
          "Classes after adding admin:",
          messageElement.classList.toString()
        );
      } else {
        // Message from restaurant owner or customer
        console.log("Not admin message, senderRole:", message.senderRole);
        if (
          message.senderRole === "RESTAURANT_OWNER" ||
          message.senderRole === "restaurant_owner"
        ) {
          console.log("Adding restaurant-owner-message class");
          messageElement.classList.add("restaurant-owner-message");
          console.log(
            "Classes after adding restaurant-owner:",
            messageElement.classList.toString()
          );
        } else if (
          message.senderRole === "CUSTOMER" ||
          message.senderRole === "customer"
        ) {
          console.log("Adding customer-message class");
          messageElement.classList.add("customer-message");
          console.log(
            "Classes after adding customer:",
            messageElement.classList.toString()
          );
        } else {
          console.log("No role class added, senderRole:", message.senderRole);
        }
      }

      console.log("Final classes:", messageElement.classList.toString());
      console.log("messageElement after adding classes:", messageElement);
    } catch (error) {
      console.error("Error adding classes:", error);
    }

    messagesContainer.appendChild(messageItem);

    // Scroll to bottom
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
  }

  async sendMessage() {
    const messageInput = document.getElementById("message-input");
    if (!messageInput || !this.currentRoomId) return;

    const content = messageInput.value.trim();
    if (!content) return;

    try {
      console.log("Sending message:", {
        roomId: this.currentRoomId,
        content: content,
        isConnected: this.isConnected,
      });

      const message = {
        roomId: this.currentRoomId,
        content: content,
        messageType: "TEXT",
      };

      if (this.stompClient && this.isConnected) {
        this.stompClient.send(
          "/app/chat.sendMessage",
          {},
          JSON.stringify(message)
        );
        messageInput.value = "";
        console.log("✅ Message sent successfully");
      } else {
        throw new Error("WebSocket not connected");
      }
    } catch (error) {
      console.error("❌ Error sending message:", error);
      this.showError("Không thể gửi tin nhắn: " + error.message);
    }
  }

  // Update room list (for unread count updates)
  async updateRoomList() {
    try {
      const response = await fetch("/api/chat/rooms");
      if (response.ok) {
        const rooms = await response.json();
        // Update existing restaurant items with room data
        rooms.forEach((room) => {
          this.updateRestaurantItemWithRoomData(room);
        });
        // Update total unread badge
        this.updateTotalUnreadBadge(rooms);
      }
    } catch (error) {
      console.error("Error updating room list:", error);
    }
  }

  // Display rooms in sidebar (update existing restaurant items)
  displayRooms(rooms) {
    // Update existing restaurant items with room data
    rooms.forEach((room) => {
      this.updateRestaurantItemWithRoomData(room);
    });
  }

  // Update restaurant item with room data
  updateRestaurantItemWithRoomData(room) {
    const restaurantItem = document.querySelector(
      `[data-restaurant-id="${room.restaurantId}"]`
    );
    if (restaurantItem) {
      // Add room ID to restaurant item
      restaurantItem.dataset.roomId = room.roomId;

      // Update unread count
      const unreadElement = restaurantItem.querySelector(".restaurant-unread");
      if (room.unreadCount && room.unreadCount > 0) {
        if (unreadElement) {
          unreadElement.textContent = room.unreadCount;
          unreadElement.style.display = "flex";
        } else {
          // Create unread badge if it doesn't exist
          const restaurantMeta =
            restaurantItem.querySelector(".restaurant-meta");
          if (restaurantMeta) {
            const unreadBadge = document.createElement("span");
            unreadBadge.className = "restaurant-unread";
            unreadBadge.textContent = room.unreadCount;
            restaurantMeta.appendChild(unreadBadge);
          }
        }
      } else {
        if (unreadElement) {
          unreadElement.style.display = "none";
        }
      }
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

  // Scroll to bottom
  scrollToBottom() {
    const messagesContainer = document.getElementById("messages-container");
    if (messagesContainer) {
      messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
  }

  formatTime(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleTimeString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  showError(message) {
    // Simple error display - could be enhanced with toast notifications
    console.error("Error:", message);
    alert(message);
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

// Initialize chat manager when DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
  window.chatManager = new AdminChatManager();
});
