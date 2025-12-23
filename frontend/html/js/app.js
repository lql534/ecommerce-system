/**
 * 主应用逻辑
 */
let currentPage = 1;
let pageSize = 12;
let totalPages = 1;

document.addEventListener('DOMContentLoaded', () => {
    initApp();
    loadProducts();
    setupEventListeners();
});

function initApp() {
    updateUserNav();
    updateCartCount();
}

function updateUserNav() {
    const userNav = document.getElementById('userNav');
    const adminBtn = document.getElementById('adminBtn');
    if (!userNav) return;

    const user = userStore.getUser();
    if (user) {
        userNav.innerHTML = `
            <span style="color:#fff;margin-right:10px;">👤 ${user.username}</span>
            <a href="#" onclick="logout()">退出</a>
        `;
        if (adminBtn && user.role === 'ADMIN') {
            adminBtn.innerHTML = `
                <button onclick="openAddModal()" class="btn btn-primary">+ 添加商品</button>
                <a href="admin.html" class="btn btn-secondary" style="margin-left:10px;">管理后台</a>
            `;
        }
    } else {
        userNav.innerHTML = `<a href="#" onclick="showLoginModal()">登录</a>`;
        if (adminBtn) adminBtn.innerHTML = '';
    }
}

async function updateCartCount() {
    const badge = document.getElementById('cartCount');
    if (!badge) return;
    
    const user = userStore.getUser();
    if (!user) {
        badge.textContent = '0';
        return;
    }
    
    try {
        const items = await api.getCart(user.id);
        const count = items.reduce((sum, item) => sum + item.quantity, 0);
        badge.textContent = count;
    } catch (e) {
        badge.textContent = '0';
    }
}

function setupEventListeners() {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        let debounceTimer;
        searchInput.addEventListener('input', () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                currentPage = 1;
                loadProducts();
            }, 300);
        });
    }

    const categoryFilter = document.getElementById('categoryFilter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', () => {
            currentPage = 1;
            loadProducts();
        });
    }
}

async function loadProducts() {
    const productList = document.getElementById('productList');
    if (!productList) return;

    productList.innerHTML = '<div class="loading">加载中...</div>';

    try {
        const keyword = document.getElementById('searchInput')?.value || '';
        const category = document.getElementById('categoryFilter')?.value || '';

        const params = { page: currentPage - 1, size: pageSize };
        if (keyword) params.keyword = keyword;
        if (category) params.category = category;

        const data = await api.getProducts(params);
        
        let products = [];
        if (Array.isArray(data)) {
            products = data;
            totalPages = 1;
        } else if (data.content) {
            products = data.content;
            totalPages = data.totalPages || 1;
        }

        renderProducts(products);
        renderPagination();
    } catch (error) {
        productList.innerHTML = `<div class="error">加载失败: ${error.message}</div>`;
    }
}

function renderProducts(products) {
    const productList = document.getElementById('productList');
    const isAdmin = userStore.isAdmin();
    const isLoggedIn = userStore.isLoggedIn();
    
    if (!products || products.length === 0) {
        productList.innerHTML = '<div class="loading">暂无商品数据</div>';
        return;
    }

    productList.innerHTML = products.map(product => `
        <div class="product-card">
            <img src="${product.imageUrl || '/images/default.jpg'}" 
                 alt="${product.name}" 
                 class="product-image"
                 onerror="this.src='/images/default.jpg'">
            <div class="product-info">
                <h3 class="product-name">${escapeHtml(product.name)}</h3>
                <p class="product-category">
                    <span class="category-tag">${escapeHtml(product.category || '未分类')}</span>
                </p>
                <p class="product-price">¥${product.price?.toFixed(2) || '0.00'}</p>
                <p class="product-stock">
                    ${product.stock > 0 
                        ? `<span class="in-stock">库存: ${product.stock}</span>` 
                        : '<span class="out-of-stock">缺货</span>'}
                </p>
                <div class="product-actions">
                    <button onclick="viewProduct(${product.id})" class="btn btn-primary btn-sm">查看</button>
                    ${isLoggedIn && product.stock > 0 ? 
                        `<button onclick="addToCart(${product.id})" class="btn btn-secondary btn-sm">加购</button>` : ''}
                    ${isAdmin ? `
                        <button onclick="openEditModal(${product.id})" class="btn btn-secondary btn-sm">编辑</button>
                        <button onclick="deleteProduct(${product.id})" class="btn btn-danger btn-sm">删除</button>
                    ` : ''}
                </div>
            </div>
        </div>
    `).join('');
}

function renderPagination() {
    const pagination = document.getElementById('pagination');
    if (!pagination || totalPages <= 1) {
        if (pagination) pagination.innerHTML = '';
        return;
    }

    let html = `<button onclick="goToPage(${currentPage - 1})" ${currentPage === 1 ? 'disabled' : ''}>上一页</button>`;
    
    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= currentPage - 2 && i <= currentPage + 2)) {
            html += `<button onclick="goToPage(${i})" class="${i === currentPage ? 'active' : ''}">${i}</button>`;
        } else if (i === currentPage - 3 || i === currentPage + 3) {
            html += '<button disabled>...</button>';
        }
    }
    
    html += `<button onclick="goToPage(${currentPage + 1})" ${currentPage === totalPages ? 'disabled' : ''}>下一页</button>`;
    pagination.innerHTML = html;
}

function goToPage(page) {
    if (page < 1 || page > totalPages) return;
    currentPage = page;
    loadProducts();
}

function viewProduct(id) {
    window.location.href = `detail.html?id=${id}`;
}

// ==================== 购物车操作 ====================
async function addToCart(productId) {
    const user = userStore.getUser();
    if (!user) {
        showLoginModal();
        return;
    }

    try {
        await api.addToCart(user.id, productId, 1);
        alert('已添加到购物车！');
        updateCartCount();
    } catch (error) {
        alert(error.message);
    }
}

// ==================== 登录相关 ====================
function showLoginModal() {
    document.getElementById('loginModal').classList.add('show');
}

function closeLoginModal() {
    document.getElementById('loginModal').classList.remove('show');
}

async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    try {
        const user = await api.login(username, password);
        userStore.setUser(user);
        closeLoginModal();
        updateUserNav();
        updateCartCount();
        loadProducts();
        alert('登录成功！');
    } catch (error) {
        alert(error.message);
    }
}

function logout() {
    userStore.clearUser();
    updateUserNav();
    updateCartCount();
    loadProducts();
}

// ==================== 商品管理（管理员） ====================
function openAddModal() {
    document.getElementById('editModalTitle').textContent = '添加商品';
    document.getElementById('editId').value = '';
    document.getElementById('editForm').reset();
    document.getElementById('editModal').classList.add('show');
}

async function openEditModal(id) {
    try {
        const product = await api.getProduct(id);
        document.getElementById('editModalTitle').textContent = '编辑商品';
        document.getElementById('editId').value = product.id;
        document.getElementById('editName').value = product.name;
        document.getElementById('editDescription').value = product.description || '';
        document.getElementById('editPrice').value = product.price;
        document.getElementById('editStock').value = product.stock;
        document.getElementById('editCategory').value = product.category || '';
        document.getElementById('editImageUrl').value = product.imageUrl || '';
        document.getElementById('editModal').classList.add('show');
    } catch (error) {
        alert('获取商品信息失败: ' + error.message);
    }
}

function closeEditModal() {
    document.getElementById('editModal').classList.remove('show');
}

async function saveProduct(event) {
    event.preventDefault();
    
    const id = document.getElementById('editId').value;
    const product = {
        name: document.getElementById('editName').value,
        description: document.getElementById('editDescription').value,
        price: parseFloat(document.getElementById('editPrice').value),
        stock: parseInt(document.getElementById('editStock').value),
        category: document.getElementById('editCategory').value,
        imageUrl: document.getElementById('editImageUrl').value || `/images/${document.getElementById('editName').value}.jpg`
    };

    try {
        if (id) {
            await api.updateProduct(id, product);
            alert('更新成功！');
        } else {
            await api.createProduct(product);
            alert('添加成功！');
        }
        closeEditModal();
        loadProducts();
    } catch (error) {
        alert('保存失败: ' + error.message);
    }
}

async function deleteProduct(id) {
    if (!confirm('确定要删除这个商品吗？')) return;
    
    try {
        await api.deleteProduct(id);
        alert('删除成功！');
        loadProducts();
    } catch (error) {
        alert('删除失败: ' + error.message);
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
