/**
 * Global Chat Widget JavaScript
 * Handles WebSocket communication and UI interactions
 */

class ChatWidget {
    constructor() {
        this.socket = null;
        this.stompClient = null;
        this.currentRoomId = null;
        this.currentUserId = null;
        this.userRole = null;
        this.isConnected = false;
        this.typingTimer = null;
        
        this.init();
    }
    
    init() {
        this.loadUserInfo();
        this.loadAvailableRestaurants();
        this.loadChatRooms();
        this.initWebSocket();
        this.setupEventListeners();
    }
    
    // Load user information from server
    async loadUserInfo() {
        try {
            const response = await fetch('/api/user/current');
            if (response.ok) {
                const user = await response.json();
                this.currentUserId = user.id;
                this.userRole = user.role;
            }
        } catch (error) {
            console.error('Failed to load user info:', error);
        }
    }
    
    // Load available restaurants for chat
    async loadAvailableRestaurants() {
        try {
            const response = await fetch('/api/chat/available-restaurants');
            console.log('Restaurants API response status:', response.status);
            
            if (response.ok) {
                const restaurants = await response.json();
                console.log('Restaurants loaded:', restaurants);
                this.populateRestaurantDropdown(restaurants);
            } else {
                const errorText = await response.text();
                console.error('API Error:', response.status, errorText);
                this.showError('Không thể tải danh sách nhà hàng: ' + errorText);
            }
        } catch (error) {
            console.error('Failed to load restaurants:', error);
            this.showError('Lỗi kết nối khi tải nhà hàng: ' + error.message);
        }
    }
    
    // Populate restaurant dropdown
    populateRestaurantDropdown(restaurants) {
        const dropdown = document.getElementById('restaurant-dropdown');
        dropdown.innerHTML = '<option value="">Chọn nhà hàng...</option>';
        
        restaurants.forEach(restaurant => {
            const option = document.createElement('option');
            option.value = restaurant.restaurantId;
            option.textContent = restaurant.restaurantName;
            dropdown.appendChild(option);
        });
    }
    
    // Load user's chat rooms
    async loadChatRooms() {
        try {
            const response = await fetch('/api/chat/rooms');
            console.log('Chat rooms API response status:', response.status);
            
            if (response.ok) {
                const rooms = await response.json();
                console.log('Chat rooms loaded:', rooms);
                this.populateChatRooms(rooms);
                this.updateUnreadBadge();
            } else {
                const errorText = await response.text();
                console.error('Chat rooms API Error:', response.status, errorText);
                this.showError('Không thể tải danh sách chat: ' + errorText);
            }
        } catch (error) {
            console.error('Failed to load chat rooms:', error);
            this.showError('Lỗi kết nối khi tải chat: ' + error.message);
        }
    }
    
    // Populate chat rooms list
    populateChatRooms(rooms) {
        const roomsList = document.getElementById('rooms-list');
        roomsList.innerHTML = '';
        
        if (rooms.length === 0) {
            roomsList.innerHTML = '<div class="text-center text-muted p-4">Chưa có cuộc trò chuyện nào</div>';
            return;
        }
        
        rooms.forEach(room => {
            const roomElement = this.createRoomElement(room);
            roomsList.appendChild(roomElement);
        });
    }
    
    // Create room element from template
    createRoomElement(room) {
        const template = document.getElementById('room-template');
        const clone = template.content.cloneNode(true);
        const roomItem = clone.querySelector('.room-item');
        
        roomItem.setAttribute('data-room-id', room.roomId);
        roomItem.querySelector('.room-name').textContent = room.restaurantName;
        roomItem.querySelector('.room-last-message').textContent = room.lastMessage || 'Chưa có tin nhắn';
        roomItem.querySelector('.room-time').textContent = this.formatTime(room.lastMessageAt);
        
        if (room.unreadCount > 0) {
            const unreadElement = roomItem.querySelector('.room-unread');
            unreadElement.textContent = room.unreadCount;
            unreadElement.style.display = 'inline-block';
        }
        
        roomItem.addEventListener('click', () => this.openChatRoom(room.roomId));
        
        return roomItem;
    }
    
    // Initialize WebSocket connection
    initWebSocket() {
        try {
            this.socket = new SockJS('/ws');
            this.stompClient = Stomp.over(this.socket);
            
            this.stompClient.connect({}, (frame) => {
                console.log('Connected to WebSocket');
                this.isConnected = true;
                this.resetReconnectAttempts(); // Reset on successful connection
                this.subscribeToUserMessages();
            }, (error) => {
                console.error('WebSocket connection error:', error);
                this.isConnected = false;
                this.reconnectWebSocket();
            });
        } catch (error) {
            console.error('Failed to initialize WebSocket:', error);
        }
    }
    
    // Subscribe to user-specific messages
    subscribeToUserMessages() {
        if (!this.isConnected) return;
        
        // Subscribe to room messages
        this.stompClient.subscribe('/topic/room/*', (message) => {
            const data = JSON.parse(message.body);
            this.handleIncomingMessage(data);
        });
        
        // Subscribe to typing indicators
        this.stompClient.subscribe('/topic/room/*/typing', (message) => {
            const data = JSON.parse(message.body);
            this.handleTypingIndicator(data);
        });
        
        // Subscribe to user-specific errors
        this.stompClient.subscribe('/user/queue/errors', (message) => {
            const data = JSON.parse(message.body);
            this.showError(data.message);
        });
    }
    
    // Reconnect WebSocket with exponential backoff
    reconnectWebSocket() {
        const maxRetries = 5;
        const baseDelay = 1000; // 1 second
        
        if (!this.reconnectAttempts) {
            this.reconnectAttempts = 0;
        }
        
        if (this.reconnectAttempts >= maxRetries) {
            console.error('Max reconnection attempts reached');
            this.showError('Không thể kết nối lại. Vui lòng tải lại trang.');
            return;
        }
        
        this.reconnectAttempts++;
        const delay = baseDelay * Math.pow(2, this.reconnectAttempts - 1); // Exponential backoff
        
        console.log(`Attempting to reconnect WebSocket (attempt ${this.reconnectAttempts}/${maxRetries}) in ${delay}ms...`);
        
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
        const messageInput = document.getElementById('message-input');
        if (messageInput) {
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                    e.preventDefault();
                    this.sendMessage();
                }
            });
            
            messageInput.addEventListener('input', () => {
                this.handleTyping();
            });
        }
        
        // Restaurant dropdown change
        const restaurantDropdown = document.getElementById('restaurant-dropdown');
        if (restaurantDropdown) {
            restaurantDropdown.addEventListener('change', () => {
                const selectedRestaurant = restaurantDropdown.value;
                const startChatBtn = document.getElementById('start-chat-btn');
                startChatBtn.disabled = !selectedRestaurant;
            });
        }
    }
    
    // Start chat with selected restaurant
    async startChatWithRestaurant() {
        const restaurantId = document.getElementById('restaurant-dropdown').value;
        if (!restaurantId) return;
        
        try {
            const response = await fetch('/api/chat/rooms', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ restaurantId: parseInt(restaurantId) })
            });
            
            if (response.ok) {
                const result = await response.json();
                this.openChatRoom(result.roomId);
                this.loadChatRooms(); // Refresh rooms list
            } else {
                const error = await response.text();
                this.showError(error);
            }
        } catch (error) {
            console.error('Failed to create chat room:', error);
            this.showError('Không thể tạo cuộc trò chuyện');
        }
    }
    
    // Open chat room
    async openChatRoom(roomId) {
        this.currentRoomId = roomId;
        
        // Hide restaurant selector and rooms list
        document.getElementById('restaurant-selector').style.display = 'none';
        document.getElementById('chat-rooms').style.display = 'none';
        
        // Show chat interface
        document.getElementById('chat-interface').style.display = 'flex';
        
        // Load messages
        await this.loadMessages(roomId);
        
        // Mark messages as read
        await this.markMessagesAsRead(roomId);
        
        // Join room via WebSocket
        this.joinRoom(roomId);
        
        // Update room name
        this.updateRoomInfo(roomId);
    }
    
    // Load messages for a room
    async loadMessages(roomId) {
        try {
            const response = await fetch(`/api/chat/rooms/${roomId}/messages?page=0&size=50`);
            if (response.ok) {
                const messages = await response.json();
                this.displayMessages(messages);
            }
        } catch (error) {
            console.error('Failed to load messages:', error);
        }
    }
    
    // Display messages in chat interface
    displayMessages(messages) {
        const container = document.getElementById('messages-container');
        container.innerHTML = '';
        
        messages.forEach(message => {
            const messageElement = this.createMessageElement(message);
            container.appendChild(messageElement);
        });
        
        this.scrollToBottom();
    }
    
    // Create message element from template
    createMessageElement(message) {
        const template = document.getElementById('message-template');
        const clone = template.content.cloneNode(true);
        const messageItem = clone.querySelector('.message-item');
        
        messageItem.setAttribute('data-message-id', message.messageId);
        
        // Safe handling of senderName
        const senderName = message.senderName || 'Người dùng';
        messageItem.querySelector('.message-sender').textContent = senderName;
        
        // Safe handling of sentAt
        const timeText = this.formatTime(message.sentAt);
        messageItem.querySelector('.message-time').textContent = timeText;
        
        // Safe handling of content
        const content = message.content || '';
        messageItem.querySelector('.message-text').textContent = content;
        
        // Check if message is from current user
        if (message.senderId === this.currentUserId) {
            messageItem.classList.add('own-message');
        }
        
        return messageItem;
    }
    
    // Send message
    async sendMessage() {
        const messageInput = document.getElementById('message-input');
        const content = messageInput.value.trim();
        
        if (!content || !this.currentRoomId || !this.isConnected) {
            this.showError('Không thể gửi tin nhắn: ' + 
                (!content ? 'Nội dung trống' : 
                 !this.currentRoomId ? 'Chưa chọn phòng chat' : 
                 'Chưa kết nối WebSocket'));
            return;
        }
        
        // Validate message length
        if (content.length > 1000) {
            this.showError('Tin nhắn quá dài (tối đa 1000 ký tự)');
            return;
        }
        
        try {
            this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
                roomId: this.currentRoomId,
                content: content
            }));
            
            messageInput.value = '';
            this.stopTyping();
        } catch (error) {
            console.error('Failed to send message:', error);
            this.showError('Không thể gửi tin nhắn. Vui lòng thử lại.');
        }
    }
    
    // Handle incoming message
    handleIncomingMessage(data) {
        // Only process valid chat messages (not join notifications or other events)
        if (!data || !data.messageId || !data.content || !data.senderName || !data.sentAt) {
            console.log('Ignoring non-message data:', data);
            return;
        }
        
        if (data.roomId === this.currentRoomId) {
            // Add message to current chat
            const messageElement = this.createMessageElement(data);
            document.getElementById('messages-container').appendChild(messageElement);
            this.scrollToBottom();
        }
        
        // Update rooms list
        this.loadChatRooms();
    }
    
    // Handle typing indicator
    handleTypingIndicator(data) {
        if (data.userId === this.currentUserId) return;
        
        const typingIndicator = document.getElementById('typing-indicator');
        if (data.typing) {
            typingIndicator.style.display = 'flex';
        } else {
            typingIndicator.style.display = 'none';
        }
    }
    
    // Handle typing
    handleTyping() {
        if (!this.currentRoomId || !this.isConnected) return;
        
        // Send typing indicator
        this.stompClient.send('/app/chat.typing', {}, JSON.stringify({
            roomId: this.currentRoomId,
            typing: true
        }));
        
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
        
        this.stompClient.send('/app/chat.typing', {}, JSON.stringify({
            roomId: this.currentRoomId,
            typing: false
        }));
        
        if (this.typingTimer) {
            clearTimeout(this.typingTimer);
            this.typingTimer = null;
        }
    }
    
    // Join room
    joinRoom(roomId) {
        if (!this.isConnected) return;
        
        this.stompClient.send('/app/chat.joinRoom', {}, JSON.stringify({
            roomId: roomId
        }));
    }
    
    // Mark messages as read
    async markMessagesAsRead(roomId) {
        try {
            await fetch(`/api/chat/rooms/${roomId}/read`, { method: 'POST' });
        } catch (error) {
            console.error('Failed to mark messages as read:', error);
        }
    }
    
    // Update unread badge
    async updateUnreadBadge() {
        try {
            const response = await fetch('/api/chat/unread-count');
            if (response.ok) {
                const data = await response.json();
                const badge = document.getElementById('unread-badge');
                
                if (data.unreadCount > 0) {
                    badge.textContent = data.unreadCount;
                    badge.style.display = 'inline-block';
                } else {
                    badge.style.display = 'none';
                }
            }
        } catch (error) {
            console.error('Failed to update unread badge:', error);
        }
    }
    
    // Update room info
    updateRoomInfo(roomId) {
        // This would typically fetch room details from server
        document.getElementById('current-room-name').textContent = 'Chat Room';
        document.getElementById('current-room-status').textContent = 'Online';
    }
    
    // Scroll to bottom of messages
    scrollToBottom() {
        const container = document.getElementById('messages-container');
        container.scrollTop = container.scrollHeight;
    }
    
    // Format time for display
    formatTime(dateTimeString) {
        // Handle invalid or null dateTimeString
        if (!dateTimeString) {
            return 'Thời gian không xác định';
        }
        
        const date = new Date(dateTimeString);
        
        // Check if date is valid
        if (isNaN(date.getTime())) {
            return 'Thời gian không hợp lệ';
        }
        
        const now = new Date();
        const diff = now - date;
        
        if (diff < 60000) { // Less than 1 minute
            return 'Vừa xong';
        } else if (diff < 3600000) { // Less than 1 hour
            return Math.floor(diff / 60000) + ' phút trước';
        } else if (diff < 86400000) { // Less than 1 day
            return Math.floor(diff / 3600000) + ' giờ trước';
        } else {
            return date.toLocaleDateString('vi-VN');
        }
    }
    
    // Show error message with better UI
    showError(message) {
        console.error('Chat Error:', message);
        
        // Create or update error notification
        let errorDiv = document.getElementById('chat-error-notification');
        if (!errorDiv) {
            errorDiv = document.createElement('div');
            errorDiv.id = 'chat-error-notification';
            errorDiv.className = 'chat-error-notification';
            errorDiv.innerHTML = `
                <div class="error-content">
                    <i class="fas fa-exclamation-triangle"></i>
                    <span class="error-message"></span>
                    <button class="error-close" onclick="this.parentElement.parentElement.remove()">
                        <i class="fas fa-times"></i>
                    </button>
                </div>
            `;
            
            // Insert at the top of chat panel
            const chatPanel = document.getElementById('chat-panel');
            chatPanel.insertBefore(errorDiv, chatPanel.firstChild);
        }
        
        // Update error message
        errorDiv.querySelector('.error-message').textContent = message;
        
        // Auto-hide after 5 seconds
        setTimeout(() => {
            if (errorDiv && errorDiv.parentNode) {
                errorDiv.remove();
            }
        }, 5000);
    }
}

// Global functions for HTML onclick events
let chatWidget;

function toggleChatWidget() {
    const panel = document.getElementById('chat-panel');
    if (panel.style.display === 'none') {
        panel.style.display = 'flex';
        if (chatWidget) {
            chatWidget.loadChatRooms();
        }
    } else {
        panel.style.display = 'none';
    }
}

function startChatWithRestaurant() {
    if (chatWidget) {
        chatWidget.startChatWithRestaurant();
    }
}

function showRestaurantSelector() {
    document.getElementById('restaurant-selector').style.display = 'flex';
    document.getElementById('chat-rooms').style.display = 'none';
    document.getElementById('chat-interface').style.display = 'none';
}

function backToRooms() {
    document.getElementById('restaurant-selector').style.display = 'none';
    document.getElementById('chat-rooms').style.display = 'flex';
    document.getElementById('chat-interface').style.display = 'none';
    
    if (chatWidget) {
        chatWidget.currentRoomId = null;
    }
}

function sendMessage() {
    if (chatWidget) {
        chatWidget.sendMessage();
    }
}

// Initialize chat widget when page loads
document.addEventListener('DOMContentLoaded', () => {
    chatWidget = new ChatWidget();
});
