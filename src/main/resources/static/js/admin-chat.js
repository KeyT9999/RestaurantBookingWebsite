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
    }

    loadAdminInfo() {
        // Try to get admin info from page data first
        const adminContainer = document.querySelector('.admin-chat-container');
        if (adminContainer && adminContainer.dataset.adminId) {
            this.adminId = adminContainer.dataset.adminId;
            console.log('Admin ID from template:', this.adminId);
        } else {
            // Fallback: load from API
            this.loadAdminFromAPI();
        }
    }
    
    async loadAdminFromAPI() {
        try {
            const response = await fetch('/api/user/current');
            if (response.ok) {
                const user = await response.json();
                this.adminId = user.id;
                console.log('Admin ID from API:', this.adminId);
            }
        } catch (error) {
            console.error('Failed to load admin info:', error);
        }
    }

    initWebSocket() {
        try {
            console.log('Initializing WebSocket connection...');
            this.socket = new SockJS('/ws');
            this.stompClient = Stomp.over(this.socket);
            
            this.stompClient.debug = (str) => {
                console.log('STOMP Debug:', str);
            };
            
            this.stompClient.connect({}, (frame) => {
                console.log('✅ Connected to WebSocket:', frame);
                this.isConnected = true;
                this.subscribeToMessages();
            }, (error) => {
                console.error('❌ WebSocket connection error:', error);
                this.showError('Lỗi kết nối WebSocket: ' + error);
            });
        } catch (error) {
            console.error('❌ Error initializing WebSocket:', error);
            this.showError('Không thể khởi tạo WebSocket: ' + error);
        }
    }

    subscribeToMessages() {
        if (this.stompClient && this.isConnected) {
            // Only subscribe to general messages topic for backward compatibility
            this.stompClient.subscribe('/topic/messages', (message) => {
                try {
                    const data = JSON.parse(message.body);
                    console.log('Received general message:', data);
                    // Only handle if it's for current room
                    if (data.roomId === this.currentRoomId) {
                        this.handleIncomingMessage(data);
                    }
                } catch (error) {
                    console.error('Error parsing general message:', error);
                }
            });
        }
    }

    subscribeToRoom(roomId) {
        if (this.stompClient && this.isConnected) {
            // Subscribe to specific room messages
            const roomTopic = `/topic/room/${roomId}`;
            console.log('Subscribing to room topic:', roomTopic);
            
            this.stompClient.subscribe(roomTopic, (message) => {
                try {
                    const data = JSON.parse(message.body);
                    console.log('Received room message:', data);
                    this.handleIncomingMessage(data);
                } catch (error) {
                    console.error('Error parsing room message:', error);
                }
            });
        }
    }

    handleIncomingMessage(data) {
        console.log('Received message:', data);
        
        // Only show messages for current room
        if (data.roomId === this.currentRoomId) {
            this.addMessageToChat(data);
        }
        
        // Update room list if needed
        this.updateRoomList();
    }

    setupEventListeners() {
        // Send message button
        const sendButton = document.getElementById('send-button');
        if (sendButton) {
            sendButton.addEventListener('click', () => this.sendMessage());
        }

        // Enter key in message input
        const messageInput = document.getElementById('message-input');
        if (messageInput) {
            messageInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.sendMessage();
                }
            });
        }
    }

    async loadAvailableRestaurants() {
        try {
            console.log('Loading available restaurants...');
            const response = await fetch('/api/chat/available-restaurants');
            console.log('Restaurants API response:', response.status, response.statusText);
            
            if (response.ok) {
                const restaurants = await response.json();
                console.log('Restaurants loaded:', restaurants);
                this.displayRestaurants(restaurants);
            } else {
                const errorText = await response.text();
                console.error('Failed to load restaurants:', errorText);
                throw new Error('Failed to load restaurants: ' + errorText);
            }
        } catch (error) {
            console.error('Error loading restaurants:', error);
            this.showError('Không thể tải danh sách nhà hàng: ' + error.message);
        }
    }

    displayRestaurants(restaurants) {
        const restaurantList = document.getElementById('restaurant-list');
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
        
        restaurantList.innerHTML = '';
        restaurants.forEach(restaurant => {
            const restaurantItem = this.createRestaurantItem(restaurant);
            restaurantList.appendChild(restaurantItem);
        });
    }

    createRestaurantItem(restaurant) {
        const template = document.getElementById('restaurant-item-template');
        const restaurantItem = template.content.cloneNode(true);
        
        const restaurantDiv = restaurantItem.querySelector('.restaurant-item');
        restaurantDiv.setAttribute('data-restaurant-id', restaurant.restaurantId);
        
        restaurantDiv.querySelector('.restaurant-name').textContent = restaurant.restaurantName;
        restaurantDiv.querySelector('.restaurant-owner').textContent = restaurant.ownerName;
        
        restaurantDiv.querySelector('.start-chat-btn').addEventListener('click', () => {
            this.startChatWithRestaurant(restaurant.restaurantId);
        });
        
        return restaurantDiv;
    }

    async startChatWithRestaurant(restaurantId) {
        try {
            console.log('Starting chat with restaurant:', restaurantId);
            const response = await fetch('/api/chat/rooms/restaurant', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `restaurantId=${restaurantId}`
            });
            
            console.log('Create room API response:', response.status, response.statusText);
            
            if (response.ok) {
                const result = await response.json();
                console.log('Room created:', result);
                this.openChatRoom(result.roomId);
            } else {
                const error = await response.text();
                console.error('Failed to create room:', error);
                throw new Error(error);
            }
        } catch (error) {
            console.error('Error starting chat with restaurant:', error);
            this.showError('Không thể bắt đầu chat với nhà hàng: ' + error.message);
        }
    }

    openChatRoom(roomId) {
        console.log('Opening chat room:', roomId);
        this.currentRoomId = roomId;
        
        // Subscribe to room-specific messages only
        this.subscribeToRoom(roomId);
        
        // Hide welcome message
        const chatWelcome = document.getElementById('chat-welcome');
        if (chatWelcome) {
            chatWelcome.style.display = 'none';
        }
        
        // Show chat messages and input
        const chatMessages = document.getElementById('chat-messages');
        const chatInput = document.getElementById('chat-input');
        if (chatMessages) {
            chatMessages.style.display = 'flex';
        }
        if (chatInput) {
            chatInput.style.display = 'block';
        }
        
        // Load messages for this room
        this.loadMessages(roomId);
        
        // Update room list
        this.updateRoomList();
    }

    async loadMessages(roomId) {
        try {
            const response = await fetch(`/api/chat/rooms/${roomId}/messages?page=0&size=50`);
            if (response.ok) {
                const messages = await response.json();
                this.displayMessages(messages);
            } else {
                throw new Error('Failed to load messages');
            }
        } catch (error) {
            console.error('Error loading messages:', error);
            this.showError('Không thể tải tin nhắn');
        }
    }

    displayMessages(messages) {
        const messagesContainer = document.getElementById('messages-container');
        if (!messagesContainer) return;
        
        messagesContainer.innerHTML = '';
        messages.forEach(message => {
            this.addMessageToChat(message);
        });
        
        // Scroll to bottom
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    addMessageToChat(message) {
        const messagesContainer = document.getElementById('messages-container');
        if (!messagesContainer) return;
        
        const template = document.getElementById('message-template');
        const messageItem = template.content.cloneNode(true);
        
        // Get the actual DOM element from the DocumentFragment
        const messageElement = messageItem.querySelector('.message-item');
        if (!messageElement) {
            console.error('Message template does not contain .message-item element');
            console.log('Available elements in template:', messageItem.children);
            return;
        }
        
        console.log('Found messageElement:', messageElement);
        console.log('messageElement.classList before:', messageElement.classList.toString());
        
        messageElement.querySelector('.sender-name').textContent = message.senderName;
        messageElement.querySelector('.message-text').textContent = message.content;
        messageElement.querySelector('.message-time').textContent = this.formatTime(message.sentAt);
        
        // Debug logging
        console.log('=== DEBUG ADMIN MESSAGE ===');
        console.log('Message:', message);
        console.log('Admin ID:', this.adminId);
        console.log('Sender ID:', message.senderId);
        console.log('Sender Role:', message.senderRole);
        
        // Add role-based classes for better styling
        try {
            console.log('Comparing senderId:', message.senderId, 'with adminId:', this.adminId);
            console.log('Are they equal?', message.senderId === this.adminId);
            
            if (message.senderId === this.adminId) {
                // Message from current admin
                console.log('Adding admin-message class');
                messageElement.classList.add('own-message', 'admin-message');
                console.log('Classes after adding admin:', messageElement.classList.toString());
            } else {
                // Message from restaurant owner or customer
                console.log('Not admin message, senderRole:', message.senderRole);
                if (message.senderRole === 'RESTAURANT_OWNER' || message.senderRole === 'restaurant_owner') {
                    console.log('Adding restaurant-owner-message class');
                    messageElement.classList.add('restaurant-owner-message');
                    console.log('Classes after adding restaurant-owner:', messageElement.classList.toString());
                } else if (message.senderRole === 'CUSTOMER' || message.senderRole === 'customer') {
                    console.log('Adding customer-message class');
                    messageElement.classList.add('customer-message');
                    console.log('Classes after adding customer:', messageElement.classList.toString());
                } else {
                    console.log('No role class added, senderRole:', message.senderRole);
                }
            }
            
            console.log('Final classes:', messageElement.classList.toString());
            console.log('messageElement after adding classes:', messageElement);
        } catch (error) {
            console.error('Error adding classes:', error);
        }
        
        messagesContainer.appendChild(messageItem);
        
        // Scroll to bottom
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }

    async sendMessage() {
        const messageInput = document.getElementById('message-input');
        if (!messageInput || !this.currentRoomId) return;
        
        const content = messageInput.value.trim();
        if (!content) return;
        
        try {
            console.log('Sending message:', {
                roomId: this.currentRoomId,
                content: content,
                isConnected: this.isConnected
            });
            
            const message = {
                roomId: this.currentRoomId,
                content: content,
                messageType: 'TEXT'
            };
            
            if (this.stompClient && this.isConnected) {
                this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
                messageInput.value = '';
                console.log('✅ Message sent successfully');
            } else {
                throw new Error('WebSocket not connected');
            }
        } catch (error) {
            console.error('❌ Error sending message:', error);
            this.showError('Không thể gửi tin nhắn: ' + error.message);
        }
    }

    updateRoomList() {
        // This would typically load active chat rooms
        // For now, we'll keep it simple
        console.log('Updating room list...');
    }

    formatTime(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleTimeString('vi-VN', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    showError(message) {
        // Simple error display - could be enhanced with toast notifications
        console.error('Error:', message);
        alert(message);
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new AdminChatManager();
});
