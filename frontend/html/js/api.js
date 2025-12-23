/**
 * API 服务模块
 */
var API_BASE_URL = '/api';

var api = {
    // ==================== 商品接口 ====================
    getProducts: function(params) {
        params = params || {};
        var queryString = new URLSearchParams(params).toString();
        var url = API_BASE_URL + '/products' + (queryString ? '?' + queryString : '');
        return fetch(url)
            .then(function(response) {
                if (!response.ok) throw new Error('获取商品列表失败: ' + response.status);
                return response.json();
            });
    },

    getProduct: function(id) {
        return fetch(API_BASE_URL + '/products/' + id)
            .then(function(response) {
                if (!response.ok) throw new Error('获取商品详情失败: ' + response.status);
                return response.json();
            });
    },

    createProduct: function(product) {
        return fetch(API_BASE_URL + '/products', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(product)
        }).then(function(response) {
            return response.json().then(function(data) {
                if (!response.ok) throw new Error(data.message || '创建商品失败');
                return data;
            });
        });
    },

    updateProduct: function(id, product) {
        return fetch(API_BASE_URL + '/products/' + id, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(product)
        }).then(function(response) {
            if (!response.ok) throw new Error('更新商品失败');
            return response.json();
        });
    },

    deleteProduct: function(id) {
        return fetch(API_BASE_URL + '/products/' + id, { method: 'DELETE' })
            .then(function(response) {
                if (!response.ok) throw new Error('删除商品失败');
            });
    },

    // ==================== 用户接口 ====================
    login: function(username, password) {
        return fetch(API_BASE_URL + '/users/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: username, password: password })
        }).then(function(response) {
            if (!response.ok) throw new Error('用户名或密码错误');
            return response.json();
        });
    },

    // ==================== 购物车接口 ====================
    getCart: function(userId) {
        return fetch(API_BASE_URL + '/cart/' + userId)
            .then(function(response) {
                if (!response.ok) throw new Error('获取购物车失败');
                return response.json();
            });
    },

    addToCart: function(userId, productId, quantity) {
        quantity = quantity || 1;
        return fetch(API_BASE_URL + '/cart/' + userId, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: productId, quantity: quantity })
        }).then(function(response) {
            return response.json().then(function(data) {
                if (!response.ok) throw new Error(data.error || '添加购物车失败');
                return data;
            });
        });
    },

    updateCartItem: function(userId, productId, quantity) {
        return fetch(API_BASE_URL + '/cart/' + userId + '/' + productId, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ quantity: quantity })
        }).then(function(response) {
            return response.json().then(function(data) {
                if (!response.ok) throw new Error(data.error || '更新购物车失败');
                return data;
            });
        });
    },

    removeFromCart: function(userId, productId) {
        return fetch(API_BASE_URL + '/cart/' + userId + '/' + productId, { method: 'DELETE' })
            .then(function(response) {
                if (!response.ok) throw new Error('移除商品失败');
                return response.json();
            });
    },

    clearCart: function(userId) {
        return fetch(API_BASE_URL + '/cart/' + userId, { method: 'DELETE' })
            .then(function(response) {
                if (!response.ok) throw new Error('清空购物车失败');
                return response.json();
            });
    },

    // ==================== 订单接口 ====================
    createOrder: function(orderData) {
        return fetch(API_BASE_URL + '/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
        }).then(function(response) {
            return response.json().then(function(data) {
                if (!response.ok) throw new Error(data.error || '创建订单失败');
                return data;
            });
        });
    },

    getOrder: function(id) {
        return fetch(API_BASE_URL + '/orders/' + id)
            .then(function(response) {
                if (!response.ok) throw new Error('获取订单失败');
                return response.json();
            });
    },

    getUserOrders: function(userId) {
        return fetch(API_BASE_URL + '/orders/user/' + userId)
            .then(function(response) {
                if (!response.ok) throw new Error('获取订单列表失败');
                return response.json();
            });
    },

    getAllOrders: function(params) {
        params = params || {};
        var queryString = new URLSearchParams(params).toString();
        return fetch(API_BASE_URL + '/orders' + (queryString ? '?' + queryString : ''))
            .then(function(response) {
                if (!response.ok) throw new Error('获取订单列表失败');
                return response.json();
            });
    },

    payOrder: function(orderId) {
        return fetch(API_BASE_URL + '/orders/' + orderId + '/pay', { method: 'POST' })
            .then(function(response) {
                return response.json().then(function(data) {
                    if (!response.ok) throw new Error(data.error || '支付失败');
                    return data;
                });
            });
    },

    shipOrder: function(orderId) {
        return fetch(API_BASE_URL + '/orders/' + orderId + '/ship', { method: 'POST' })
            .then(function(response) {
                return response.json().then(function(data) {
                    if (!response.ok) throw new Error(data.error || '发货失败');
                    return data;
                });
            });
    },

    deliverOrder: function(orderId) {
        return fetch(API_BASE_URL + '/orders/' + orderId + '/deliver', { method: 'POST' })
            .then(function(response) {
                return response.json().then(function(data) {
                    if (!response.ok) throw new Error(data.error || '确认送达失败');
                    return data;
                });
            });
    },

    cancelOrder: function(orderId) {
        return fetch(API_BASE_URL + '/orders/' + orderId + '/cancel', { method: 'POST' })
            .then(function(response) {
                return response.json().then(function(data) {
                    if (!response.ok) throw new Error(data.error || '取消订单失败');
                    return data;
                });
            });
    }
};

// 用户状态管理
var userStore = {
    getUser: function() {
        var user = localStorage.getItem('currentUser');
        return user ? JSON.parse(user) : null;
    },
    setUser: function(user) {
        localStorage.setItem('currentUser', JSON.stringify(user));
    },
    clearUser: function() {
        localStorage.removeItem('currentUser');
    },
    isLoggedIn: function() {
        return this.getUser() !== null;
    },
    isAdmin: function() {
        var user = this.getUser();
        return user && user.role === 'ADMIN';
    }
};
