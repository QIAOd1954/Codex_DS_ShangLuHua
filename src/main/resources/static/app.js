const state = {
  products: [],
  customers: [],
  orders: [],
  productFilter: "",
  user: null,
  token: localStorage.getItem("token"),
};

const productImages = [
  "/assets/product-shirt.png",
  "/assets/product-dress.png",
  "/assets/product-set.png",
  "/assets/hero-look.png",
];

const $ = (id) => document.getElementById(id);

async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
  if (state.token) {
    headers["Authorization"] = "Bearer " + state.token;
  }
  const response = await fetch(path, { headers, ...options });
  if (response.status === 401) {
    state.token = null;
    localStorage.removeItem("token");
    state.user = null;
    showLogin();
    throw new Error("登录已过期，请重新登录");
  }
  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const body = await response.json();
      message = body.message || message;
    } catch (_) {}
    throw new Error(message);
  }
  if (response.status === 204) return null;
  return response.json();
}

function money(value) {
  return Number(value || 0).toFixed(2);
}

function splitCsv(value, fallback) {
  const list = String(value || "")
    .split(/[,，、]/)
    .map((item) => item.trim())
    .filter(Boolean);
  return list.length ? list : fallback;
}

function toast(message) {
  const node = $("toast");
  node.textContent = message;
  node.classList.add("show");
  window.clearTimeout(toast.timer);
  toast.timer = window.setTimeout(() => node.classList.remove("show"), 2200);
}

// ======================== Auth ========================

async function checkAuth() {
  if (!state.token) {
    showLogin();
    return false;
  }
  try {
    const me = await api("/api/auth/me");
    state.user = me;
    showApp();
    return true;
  } catch (e) {
    state.token = null;
    localStorage.removeItem("token");
    showLogin();
    return false;
  }
}

function showLogin() {
  $("loginOverlay").classList.remove("hidden");
  $("loginBtn").style.display = "none";
  $("logoutBtn").style.display = "none";
}

function showApp() {
  $("loginOverlay").classList.add("hidden");
  $("loginBtn").style.display = "none";
  $("logoutBtn").style.display = "";
  $("logoutBtn").textContent = `退出 ${state.user?.displayName || ""}`;
  refreshAll();
}

async function handleLogin() {
  const username = $("loginUsername").value.trim();
  const password = $("loginPassword").value.trim();
  $("loginError").textContent = "";
  if (!username || !password) {
    $("loginError").textContent = "请输入用户名和密码";
    return;
  }
  try {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password }),
    });
    if (!res.ok) {
      const err = await res.json();
      $("loginError").textContent = err.message || "登录失败";
      return;
    }
    const data = await res.json();
    state.token = data.token;
    localStorage.setItem("token", data.token);
    $("loginPassword").value = "";
    await checkAuth();
  } catch (e) {
    $("loginError").textContent = e.message || "网络错误";
  }
}

function handleLogout() {
  state.token = null;
  localStorage.removeItem("token");
  state.user = null;
  $("loginUsername").value = "admin";
  $("loginPassword").value = "";
  $("loginError").textContent = "";
  showLogin();
}

// ======================== Products ========================

async function loadProducts() {
  const keyword = $("productSearch").value.trim() || state.productFilter;
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : "";
  state.products = await api(`/api/products${query}`);
  renderProducts();
  renderStats();
}

function renderStats() {
  $("productCount").textContent = state.products.length;
  $("statProducts").textContent = state.products.length;
  $("statSkus").textContent = state.products.reduce((sum, product) => sum + (product.skus?.length || 0), 0);
  $("statCustomers").textContent = state.customers.length;
}

function renderProducts() {
  const grid = $("productGrid");
  if (!state.products.length) {
    grid.innerHTML = `<div class="product-card"><div class="product-info"><p class="code">EMPTY</p><h3>暂无商品</h3><p class="meta">请新增一个商品</p></div></div>`;
    return;
  }
  grid.innerHTML = state.products.map((product, index) => {
    const colors = [...new Set((product.skus || []).map((sku) => sku.colorName).filter(Boolean))].join(" / ") || "-";
    const sizes = [...new Set((product.skus || []).map((sku) => sku.sizeName).filter(Boolean))].join(" / ") || "-";
    return `
      <article class="product-card">
        <img src="${productImages[index % productImages.length]}" alt="${product.name}" />
        <div class="product-info">
          <p class="code">${product.code || ""} ／ ${product.category || ""}</p>
          <h3>${product.name || ""}</h3>
          <div class="meta"><span>${colors}</span><span>${sizes}</span><span>${product.skus?.length || 0} SKU</span></div>
          <div class="price-row">
            <strong>¥${money(product.wholesalePrice)}</strong>
            <div class="row-actions">
              <button type="button" data-edit-product="${product.id}">编辑</button>
              <button type="button" class="danger" data-delete-product="${product.id}">删除</button>
            </div>
          </div>
        </div>
      </article>`;
  }).join("");
}

function resetProductForm() {
  $("productForm").reset();
  $("productId").value = "";
  $("retailPrice").value = "99";
  $("wholesalePrice").value = "49";
  $("costPrice").value = "28";
  $("productFormTitle").textContent = "新增商品";
}

function fillProductForm(product) {
  $("productId").value = product.id;
  $("code").value = product.code || "";
  $("name").value = product.name || "";
  $("category").value = product.category || "";
  $("season").value = product.season || "";
  $("supplierName").value = product.supplierName || "";
  $("retailPrice").value = product.retailPrice || 0;
  $("wholesalePrice").value = product.wholesalePrice || 0;
  $("costPrice").value = product.costPrice || 0;
  const colors = [...new Set((product.skus || []).map((sku) => sku.colorName).filter(Boolean))].join(",");
  const sizes = [...new Set((product.skus || []).map((sku) => sku.sizeName).filter(Boolean))].join(",");
  $("colors").value = colors;
  $("sizes").value = sizes;
  $("productFormTitle").textContent = "编辑商品";
  window.scrollTo({ top: $("products").offsetTop - 100, behavior: "smooth" });
}

function productPayload() {
  const colors = splitCsv($("colors").value, ["米白", "杏黄"]);
  const sizes = splitCsv($("sizes").value, ["S", "M", "L"]);
  return {
    code: $("code").value.trim(),
    name: $("name").value.trim(),
    category: $("category").value.trim(),
    season: $("season").value.trim(),
    supplierName: $("supplierName").value.trim(),
    retailPrice: Number($("retailPrice").value || 0),
    wholesalePrice: Number($("wholesalePrice").value || 0),
    costPrice: Number($("costPrice").value || 0),
    skus: colors.flatMap((colorName) => sizes.map((sizeName) => ({
      colorName,
      sizeName,
      barcode: `${$("code").value.trim()}-${colorName}-${sizeName}`,
    }))),
  };
}

async function saveProduct(event) {
  event.preventDefault();
  const id = $("productId").value;
  const payload = productPayload();
  if (id) {
    delete payload.skus;
    await api(`/api/products/${id}`, { method: "PUT", body: JSON.stringify(payload) });
    toast("商品已更新");
  } else {
    await api("/api/products", { method: "POST", body: JSON.stringify(payload) });
    toast("商品已新增");
  }
  resetProductForm();
  await loadProducts();
}

// ======================== Customers ========================

async function loadCustomers() {
  const keyword = $("customerSearch").value.trim();
  const query = keyword ? `?keyword=${encodeURIComponent(keyword)}` : "";
  state.customers = await api(`/api/customers${query}`);
  renderCustomers();
  renderStats();
}

function renderCustomers() {
  const rows = $("customerRows");
  if (!state.customers.length) {
    rows.innerHTML = `<tr><td colspan="5">暂无客户</td></tr>`;
    return;
  }
  rows.innerHTML = state.customers.map((customer) => `
    <tr>
      <td><strong>${customer.name || ""}</strong><br><span class="code">${customer.wechat || ""}</span></td>
      <td>${customer.phone || ""}</td>
      <td>${customer.levelName || ""}</td>
      <td>¥${money(customer.debtBalance)}</td>
      <td>
        <div class="row-actions">
          <button type="button" data-edit-customer="${customer.id}">编辑</button>
          <button type="button" class="danger" data-delete-customer="${customer.id}">删除</button>
        </div>
      </td>
    </tr>
  `).join("");
}

function resetCustomerForm() {
  $("customerForm").reset();
  $("customerId").value = "";
  $("debtBalance").value = "0";
  $("customerFormTitle").textContent = "新增客户";
}

function fillCustomerForm(customer) {
  $("customerId").value = customer.id;
  $("customerName").value = customer.name || "";
  $("phone").value = customer.phone || "";
  $("wechat").value = customer.wechat || "";
  $("levelName").value = customer.levelName || "";
  $("debtBalance").value = customer.debtBalance || 0;
  $("customerFormTitle").textContent = "编辑客户";
}

function customerPayload() {
  return {
    name: $("customerName").value.trim(),
    phone: $("phone").value.trim(),
    wechat: $("wechat").value.trim(),
    levelName: $("levelName").value.trim(),
    debtBalance: Number($("debtBalance").value || 0),
  };
}

async function saveCustomer(event) {
  event.preventDefault();
  const id = $("customerId").value;
  const payload = customerPayload();
  if (id) {
    await api(`/api/customers/${id}`, { method: "PUT", body: JSON.stringify(payload) });
    toast("客户已更新");
  } else {
    await api("/api/customers", { method: "POST", body: JSON.stringify(payload) });
    toast("客户已新增");
  }
  resetCustomerForm();
  await loadCustomers();
}

// ======================== Orders ========================

const statusLabels = { DRAFT: "草稿", CONFIRMED: "已确认", CANCELED: "已取消" };
const statusBadge = { DRAFT: "badge-draft", CONFIRMED: "badge-confirmed", CANCELED: "badge-canceled" };

async function loadOrders() {
  const params = new URLSearchParams();
  const keyword = $("orderSearch").value.trim();
  const status = $("orderStatusFilter").value;
  if (keyword) params.set("keyword", keyword);
  if (status) params.set("status", status);
  const qs = params.toString();
  state.orders = await api(`/api/sales-orders${qs ? "?" + qs : ""}`);
  renderOrders();
}

function renderOrders() {
  const rows = $("orderRows");
  if (!state.orders.length) {
    rows.innerHTML = `<tr><td colspan="8">暂无订单</td></tr>`;
    return;
  }
  rows.innerHTML = state.orders.map((order) => `
    <tr>
      <td><strong>${order.orderNo || ""}</strong></td>
      <td>${order.customerName || "-"}</td>
      <td>¥${money(order.totalAmount)}</td>
      <td>¥${money(order.paidAmount)}</td>
      <td>¥${money(order.debtAmount)}</td>
      <td><span class="badge ${statusBadge[order.status] || ""}">${statusLabels[order.status] || order.status}</span></td>
      <td style="font-size:13px;color:var(--muted)">${order.createdAt ? new Date(order.createdAt).toLocaleString() : ""}</td>
      <td>
        <div class="row-actions">
          <button type="button" data-view-order="${order.id}">详情</button>
          ${order.status === "DRAFT" ? `<button type="button" class="confirm-btn-sm" data-confirm-order="${order.id}">确认</button>` : ""}
          ${order.status === "CONFIRMED" ? `<button type="button" class="danger" data-cancel-order="${order.id}">取消</button>` : ""}
        </div>
      </td>
    </tr>
  `).join("");
}

async function handleViewOrder(orderId) {
  const order = state.orders.find((o) => String(o.id) === String(orderId));
  if (!order) return;

  const overlay = document.createElement("div");
  overlay.className = "modal-overlay";
  overlay.innerHTML = `
    <div class="modal-card">
      <h3>订单详情 — ${order.orderNo}</h3>
      <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:16px;font-size:14px">
        <div><strong>客户：</strong>${order.customerName || "-"}</div>
        <div><strong>仓库：</strong>${order.warehouseCode || "MAIN"}</div>
        <div><strong>总金额：</strong>¥${money(order.totalAmount)}</div>
        <div><strong>已付：</strong>¥${money(order.paidAmount)}</div>
        <div><strong>欠款：</strong>¥${money(order.debtAmount)}</div>
        <div><strong>状态：</strong><span class="badge ${statusBadge[order.status] || ""}">${statusLabels[order.status] || order.status}</span></div>
        <div><strong>创建时间：</strong>${order.createdAt ? new Date(order.createdAt).toLocaleString() : ""}</div>
        <div><strong>确认时间：</strong>${order.confirmedAt ? new Date(order.confirmedAt).toLocaleString() : "-"}</div>
      </div>
      <table>
        <thead><tr><th>商品</th><th>颜色</th><th>尺码</th><th>数量</th><th>单价</th><th>小计</th></tr></thead>
        <tbody>
          ${(order.items || []).map((item) => `
            <tr>
              <td>${item.productName || ""}<br><small style="color:var(--muted)">${item.skuCode || ""}</small></td>
              <td>${item.colorName || ""}</td>
              <td>${item.sizeName || ""}</td>
              <td>${item.quantity || 0}</td>
              <td>¥${money(item.unitPrice)}</td>
              <td>¥${money(item.amount)}</td>
            </tr>
          `).join("") || `<tr><td colspan="6">无商品明细</td></tr>`}
        </tbody>
      </table>
      <div class="modal-actions">
        <button type="button" class="close-btn" data-close-modal>关闭</button>
        ${order.status === "DRAFT" ? `<button type="button" class="confirm-btn" data-confirm-from-modal="${order.id}">确认订单</button>` : ""}
        ${order.status === "CONFIRMED" ? `<button type="button" class="cancel-btn" data-cancel-from-modal="${order.id}">取消订单</button>` : ""}
      </div>
    </div>
  `;

  overlay.addEventListener("click", (e) => {
    if (e.target === overlay || e.target.closest("[data-close-modal]")) {
      overlay.remove();
    }
    if (e.target.closest("[data-confirm-from-modal]")) {
      const id = e.target.closest("[data-confirm-from-modal]").dataset.confirmFromModal;
      overlay.remove();
      handleConfirmOrder(id);
    }
    if (e.target.closest("[data-cancel-from-modal]")) {
      const id = e.target.closest("[data-cancel-from-modal]").dataset.cancelFromModal;
      overlay.remove();
      handleCancelOrder(id);
    }
  });

  document.body.appendChild(overlay);
}

async function handleConfirmOrder(orderId) {
  if (!confirm("确认该订单？确认后将扣减库存并累加客户欠款。")) return;
  try {
    await api(`/api/sales-orders/${orderId}/confirm`, { method: "POST" });
    toast("订单已确认");
    await loadOrders();
    await loadProducts();
  } catch (e) {
    toast(e.message || "确认失败");
  }
}

async function handleCancelOrder(orderId) {
  if (!confirm("取消该订单？取消后将恢复库存并扣减客户欠款。")) return;
  try {
    await api(`/api/sales-orders/${orderId}/cancel`, { method: "POST" });
    toast("订单已取消");
    await loadOrders();
    await loadProducts();
  } catch (e) {
    toast(e.message || "取消失败");
  }
}

// ======================== Event Handlers ========================

async function handleDocumentClick(event) {
  const productEdit = event.target.closest("[data-edit-product]");
  const productDelete = event.target.closest("[data-delete-product]");
  const customerEdit = event.target.closest("[data-edit-customer]");
  const customerDelete = event.target.closest("[data-delete-customer]");
  const viewOrder = event.target.closest("[data-view-order]");
  const confirmOrder = event.target.closest("[data-confirm-order]");
  const cancelOrder = event.target.closest("[data-cancel-order]");

  if (productEdit) {
    const product = state.products.find((item) => String(item.id) === productEdit.dataset.editProduct);
    if (product) fillProductForm(product);
  }
  if (productDelete) {
    if (!confirm("确认删除该商品？")) return;
    await api(`/api/products/${productDelete.dataset.deleteProduct}`, { method: "DELETE" });
    toast("商品已删除");
    await loadProducts();
  }
  if (customerEdit) {
    const customer = state.customers.find((item) => String(item.id) === customerEdit.dataset.editCustomer);
    if (customer) fillCustomerForm(customer);
  }
  if (customerDelete) {
    if (!confirm("确认删除该客户？")) return;
    await api(`/api/customers/${customerDelete.dataset.deleteCustomer}`, { method: "DELETE" });
    toast("客户已删除");
    await loadCustomers();
  }
  if (viewOrder) {
    handleViewOrder(viewOrder.dataset.viewOrder);
  }
  if (confirmOrder) {
    handleConfirmOrder(confirmOrder.dataset.confirmOrder);
  }
  if (cancelOrder) {
    handleCancelOrder(cancelOrder.dataset.cancelOrder);
  }
}

async function refreshAll() {
  try {
    await Promise.all([loadProducts(), loadCustomers(), loadOrders()]);
  } catch (error) {
    if (error.message !== "登录已过期，请重新登录") {
      toast(error.message || "数据加载失败");
    }
  }
}

async function init() {
  // Auth
  $("loginSubmitBtn").addEventListener("click", handleLogin);
  $("loginPassword").addEventListener("keydown", (e) => {
    if (e.key === "Enter") handleLogin();
  });
  $("loginBtn").addEventListener("click", () => {
    $("loginOverlay").classList.remove("hidden");
    $("loginUsername").focus();
  });
  $("logoutBtn").addEventListener("click", handleLogout);

  const authed = await checkAuth();
  if (!authed) return;

  // Products
  $("productForm").addEventListener("submit", (event) => saveProduct(event).catch((error) => toast(error.message)));
  $("customerForm").addEventListener("submit", (event) => saveCustomer(event).catch((error) => toast(error.message)));
  $("resetProductBtn").addEventListener("click", resetProductForm);
  $("resetCustomerBtn").addEventListener("click", resetCustomerForm);
  $("newProductBtn").addEventListener("click", resetProductForm);
  $("newCustomerBtn").addEventListener("click", resetCustomerForm);
  $("refreshBtn").addEventListener("click", refreshAll);
  $("productSearch").addEventListener("input", () => loadProducts().catch((error) => toast(error.message)));
  $("customerSearch").addEventListener("input", () => loadCustomers().catch((error) => toast(error.message)));
  $("orderSearch").addEventListener("input", () => loadOrders().catch((error) => toast(error.message)));
  $("orderStatusFilter").addEventListener("change", () => loadOrders().catch((error) => toast(error.message)));

  document.querySelectorAll(".toolbar button").forEach((button) => {
    button.addEventListener("click", () => {
      document.querySelectorAll(".toolbar button").forEach((item) => item.classList.remove("active"));
      button.classList.add("active");
      state.productFilter = button.dataset.filter || "";
      $("productSearch").value = "";
      loadProducts().catch((error) => toast(error.message));
    });
  });

  document.addEventListener("click", (event) => handleDocumentClick(event).catch((error) => toast(error.message)));

  resetProductForm();
  resetCustomerForm();
  refreshAll();
}

document.addEventListener("DOMContentLoaded", () => {
  init().catch((e) => {
    if (e.message !== "登录已过期，请重新登录") {
      toast("初始化失败: " + e.message);
    }
  });
});
