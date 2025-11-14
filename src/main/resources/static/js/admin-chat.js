class AdminChatManager {
  constructor() {
    this.adminId = null;
    this.currentRoomId = null;
    this.stompClient = null;
    this.socket = null;
    this.isConnected = false;
    
    // Infinite scroll properties
    this.currentPage = 0;
    this.pageSize = 100;
    this.hasMoreMessages = true;
    this.isLoadingMessages = false;
    this.allMessages = [];

    this.init();
  }

  init() {
    this.loadAdminInfo();
    this.initWebSocket();
    this.setupEventListeners();
    this.setupInfiniteScroll();
    this.setupRoomClickHandlers(); // Setup click handlers for chat rooms
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

  // Setup click handlers for chat room items
  setupRoomClickHandlers() {
    // Use event delegation for dynamically loaded rooms
    const chatRoomsList = document.getElementById("chat-rooms-list");
    if (chatRoomsList) {
      chatRoomsList.addEventListener("click", (e) => {
        const roomItem = e.target.closest(".chat-room-item");
        if (roomItem) {
          const roomId = roomItem.dataset.roomId;
          if (roomId) {
            this.openChatRoom(roomId);
          }
        }
      });
    }
  }

  // Show welcome screen
  showWelcomeScreen() {
    const welcomeScreen = document.getElementById("welcome-screen");
    const chatInterface = document.getElementById("chat-interface");

    if (welcomeScreen) welcomeScreen.style.display = "flex";
    if (chatInterface) chatInterface.style.display = "none";

    // Remove active class from all room items
    document.querySelectorAll(".chat-room-item").forEach((item) => {
      item.classList.remove("active");
    });
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
        // If API fails, rooms might already be loaded from server template
        this.setupRoomClickHandlers();
      }
    } catch (error) {
      console.error("Error loading chat rooms:", error);
      // If API fails, rooms might already be loaded from server template
      this.setupRoomClickHandlers();
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
  }

  // Update restaurant info in chat header
  updateRestaurantInfo(roomId) {
    // Find chat room item or restaurant item with this room ID
    const roomItem = document.querySelector(`.chat-room-item[data-room-id="${roomId}"]`);
    const restaurantItem = document.querySelector(`.restaurant-item[data-room-id="${roomId}"]`);
    
    const participantNameElement = document.getElementById("participant-name");
    
    if (roomItem) {
      const roomName = roomItem.querySelector(".room-name span");
      if (roomName && participantNameElement) {
        participantNameElement.textContent = roomName.textContent;
        console.log("Updated restaurant info from room item:", roomName.textContent);
      }
    } else if (restaurantItem) {
      const restaurantName = restaurantItem.querySelector(".restaurant-name");
      if (restaurantName && participantNameElement) {
        participantNameElement.textContent = restaurantName.textContent;
        console.log("Updated restaurant info from restaurant item:", restaurantName.textContent);
      }
    } else {
      console.log("No room item or restaurant item found for room:", roomId);
      // Try to get from API
      this.loadRoomInfo(roomId);
    }
  }

  // Load room info from API
  async loadRoomInfo(roomId) {
    try {
      const response = await fetch(`/api/chat/rooms/${roomId}`);
      if (response.ok) {
        const room = await response.json();
        const participantNameElement = document.getElementById("participant-name");
        if (participantNameElement && room.restaurantName) {
          participantNameElement.textContent = room.restaurantName;
        }
      }
    } catch (error) {
      console.error("Error loading room info:", error);
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
    const welcomeScreen = document.getElementById("welcome-screen");
    const chatInterface = document.getElementById("chat-interface");

    if (welcomeScreen) welcomeScreen.style.display = "none";
    if (chatInterface) chatInterface.style.display = "flex";
  }

  // Update room selection
  updateRoomSelection(roomId) {
    // Remove active class from all room items
    document.querySelectorAll(".chat-room-item").forEach((item) => {
      item.classList.remove("active");
    });
    document.querySelectorAll(".restaurant-item").forEach((item) => {
      item.classList.remove("active");
    });

    // Add active class to current room item
    const currentRoomItem = document.querySelector(
      `.chat-room-item[data-room-id="${roomId}"]`
    );
    if (currentRoomItem) {
      currentRoomItem.classList.add("active");
    }
    
    // Also check restaurant items
    const currentRestaurantItem = document.querySelector(
      `.restaurant-item[data-room-id="${roomId}"]`
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
      } else {
        throw new Error("Failed to load messages");
      }
    } catch (error) {
      console.error("Error loading messages:", error);
      this.showError("Không thể tải tin nhắn");
    } finally {
      this.isLoadingMessages = false;
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

  // Create message element for prepending
  createMessageElement(message) {
    const template = document.getElementById("message-template");
    const messageItem = template.content.cloneNode(true);
    const messageElement = messageItem.querySelector(".message-item");
    
    if (!messageElement) return null;

    // Set message content
    const senderElement = messageElement.querySelector(".message-sender");
    const contentElement = messageElement.querySelector(".message-content");
    const timeElement = messageElement.querySelector(".message-time");

    if (senderElement) senderElement.textContent = message.senderName || "Unknown";
    if (contentElement) contentElement.textContent = message.content || "";
    if (timeElement) {
      const sentAt = new Date(message.sentAt);
      timeElement.textContent = sentAt.toLocaleTimeString("vi-VN", {
        hour: "2-digit",
        minute: "2-digit",
      });
    }

    // Set message alignment based on sender
    const isCurrentUser = message.senderId === this.adminId;
    if (isCurrentUser) {
      messageElement.classList.add("message-right");
    } else {
      messageElement.classList.add("message-left");
    }

    return messageElement;
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

    const senderElement = messageElement.querySelector(".message-sender");
    const textElement = messageElement.querySelector(".message-text");
    const timeElement = messageElement.querySelector(".message-time");
    
    if (senderElement) {
      senderElement.textContent = message.senderName || "Unknown";
    }
    if (textElement) {
      textElement.textContent = message.content || "";
    }
    if (timeElement) {
      timeElement.textContent = this.formatTime(message.sentAt);
    }

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

  // Display rooms in sidebar (update existing restaurant items and chat room items)
  displayRooms(rooms) {
    const chatRoomsList = document.getElementById("chat-rooms-list");
    if (!chatRoomsList) return;

    // Remove empty state if rooms exist
    const emptyState = chatRoomsList.querySelector(".empty-state");
    
    // Get all existing room items (both server-rendered and dynamically added)
    const existingRoomItems = chatRoomsList.querySelectorAll(".chat-room-item[data-room-id]");
    const existingRoomIds = Array.from(existingRoomItems).map(item => item.dataset.roomId);
    
    // Update existing room items
    existingRoomItems.forEach((roomItem) => {
      const roomId = roomItem.dataset.roomId;
      const room = rooms.find(r => r.roomId === roomId);
      if (room) {
        this.updateChatRoomItem(roomItem, room);
      }
    });

    // Add new rooms that don't exist in the list
    rooms.forEach((room) => {
      if (!existingRoomIds.includes(room.roomId)) {
        const roomItem = this.createChatRoomItem(room);
        if (roomItem) {
          // Insert before empty state if it exists, otherwise append
          if (emptyState) {
            chatRoomsList.insertBefore(roomItem, emptyState);
          } else {
            chatRoomsList.appendChild(roomItem);
          }
        }
      }
      
      // Also update restaurant items with room data
      this.updateRestaurantItemWithRoomData(room);
    });

    // Show/hide empty state and section title
    const roomsSectionTitle = document.querySelector(".rooms-section-title");
    if (rooms.length > 0) {
      if (emptyState) {
        emptyState.remove();
      }
      if (roomsSectionTitle) {
        roomsSectionTitle.style.display = "flex";
      }
    } else {
      if (!emptyState) {
        // Add empty state if no rooms
        const emptyStateDiv = document.createElement("div");
        emptyStateDiv.className = "empty-state";
        emptyStateDiv.innerHTML = `
          <i class="fas fa-comments text-muted"></i>
          <p class="text-muted">Chưa có cuộc trò chuyện nào</p>
        `;
        chatRoomsList.appendChild(emptyStateDiv);
      }
      if (roomsSectionTitle) {
        roomsSectionTitle.style.display = "none";
      }
    }
  }

  // Create chat room item from template
  createChatRoomItem(room) {
    const template = document.getElementById("chat-room-item-template");
    if (!template) {
      console.error("Chat room item template not found");
      return null;
    }
    
    const roomItem = template.content.cloneNode(true);
    const roomElement = roomItem.querySelector(".chat-room-item");
    
    if (!roomElement) return null;
    
    roomElement.dataset.roomId = room.roomId;
    if (room.restaurantId) {
      roomElement.dataset.restaurantId = room.restaurantId;
    }
    
    // Update room info
    const roomName = roomElement.querySelector(".room-name span");
    if (roomName) {
      roomName.textContent = room.restaurantName || "Nhà hàng";
    }
    
    const lastMessage = roomElement.querySelector(".room-last-message");
    if (lastMessage) {
      lastMessage.textContent = room.lastMessage || "Chưa có tin nhắn";
    }
    
    const roomTime = roomElement.querySelector(".room-time");
    if (roomTime && room.lastMessageAt) {
      roomTime.textContent = this.formatTime(room.lastMessageAt);
    }
    
    const roomUnread = roomElement.querySelector(".room-unread");
    if (roomUnread) {
      if (room.unreadCount && room.unreadCount > 0) {
        roomUnread.textContent = room.unreadCount;
        roomUnread.style.display = "inline-block";
      } else {
        roomUnread.style.display = "none";
      }
    }
    
    return roomElement;
  }

  // Update existing chat room item
  updateChatRoomItem(roomItem, room) {
    // Update room name
    const roomName = roomItem.querySelector(".room-name span");
    if (roomName) {
      roomName.textContent = room.restaurantName || "Nhà hàng";
    }
    
    // Update last message
    const lastMessage = roomItem.querySelector(".room-last-message");
    if (lastMessage) {
      lastMessage.textContent = room.lastMessage || "Chưa có tin nhắn";
    }
    
    // Update time
    const roomTime = roomItem.querySelector(".room-time");
    if (roomTime) {
      if (room.lastMessageAt) {
        roomTime.textContent = this.formatTime(room.lastMessageAt);
        roomTime.style.display = "inline-block";
      } else {
        roomTime.style.display = "none";
      }
    }
    
    // Update unread count
    const roomUnread = roomItem.querySelector(".room-unread");
    if (roomUnread) {
      if (room.unreadCount && room.unreadCount > 0) {
        roomUnread.textContent = room.unreadCount;
        roomUnread.style.display = "inline-block";
      } else {
        roomUnread.style.display = "none";
      }
    }
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
    // Update chat room item
    const roomItem = document.querySelector(`.chat-room-item[data-room-id="${roomId}"]`);
    if (roomItem) {
      const lastMessageElement = roomItem.querySelector(".room-last-message");
      const timeElement = roomItem.querySelector(".room-time");

      if (lastMessageElement) {
        lastMessageElement.textContent = content;
      }

      if (timeElement) {
        timeElement.textContent = this.formatTime(sentAt);
        timeElement.style.display = "inline-block";
      }
    }
    
    // Also update restaurant item if exists
    const restaurantItem = document.querySelector(`.restaurant-item[data-room-id="${roomId}"]`);
    if (restaurantItem) {
      const lastMessageElement = restaurantItem.querySelector(".room-last-message");
      const timeElement = restaurantItem.querySelector(".room-time");

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
