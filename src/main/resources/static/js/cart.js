document.addEventListener('DOMContentLoaded', function () {
    const selectAll = document.getElementById('selectAll');
    const itemCheckboxes = document.querySelectorAll('.item-checkbox');
    const totalPriceElement = document.getElementById('totalPrice');
    const discountElement = document.getElementById('discount');
    const finalPriceElement = document.getElementById('finalPrice');
    const checkoutButton = document.getElementById('checkoutButton');
    const checkoutForm = document.getElementById('checkoutForm');
    const csrfToken = document.getElementById('csrfToken')?.value;
    const currentUserId = document.getElementById('currentUserId')?.value;

    // Chọn tất cả
    if (selectAll) {
        selectAll.addEventListener('change', function () {
            itemCheckboxes.forEach(cb => {
                cb.checked = this.checked;
            });
            updateTotals();
        });
    }

    // Chọn từng item
    itemCheckboxes.forEach(cb => {
        cb.addEventListener('change', function () {
            updateTotals();
            if (selectAll) {
                const allChecked = Array.from(itemCheckboxes).every(checkbox => checkbox.checked);
                const someChecked = Array.from(itemCheckboxes).some(checkbox => checkbox.checked);
                selectAll.checked = allChecked;
                selectAll.indeterminate = someChecked && !allChecked;
            }
        });
    });

    // Cập nhật tổng tiền
    function updateTotals() {
        let total = 0;
        let selectedCount = 0;

        itemCheckboxes.forEach(cb => {
            if (cb.checked) {
                selectedCount++;
                const price = parseFloat(cb.dataset.price) || 0;
                const quantity = parseInt(cb.dataset.quantity) || 0;
                total += price * quantity;
            }
        });

        const discountValue = parseFloat(discountElement.dataset.value) || 0;
        let discount = 0;
        if (total > 1000000) {
            discount = total * 0.1; // 10% discount
        }

        const finalPrice = total - discount;

        totalPriceElement.textContent = total.toLocaleString('vi-VN') + 'đ';
        discountElement.textContent = discount.toLocaleString('vi-VN') + 'đ';
        finalPriceElement.textContent = finalPrice.toLocaleString('vi-VN') + 'đ';

        if (checkoutButton) {
            checkoutButton.disabled = selectedCount === 0;
        }

        if (checkoutForm) {
            checkoutForm.querySelectorAll('input[name="itemIds"]').forEach(input => input.remove());
            itemCheckboxes.forEach(cb => {
                if (cb.checked) {
                    const input = document.createElement('input');
                    input.type = 'hidden';
                    input.name = 'itemIds';
                    input.value = cb.value;
                    checkoutForm.appendChild(input);
                }
            });
        }
    }

    document.querySelectorAll('.btn-add-to-cart').forEach(btn => {
        btn.addEventListener('click', function (e) {
            e.preventDefault();

            const productId = this.dataset.productId;
            const currentUserId = document.getElementById('currentUserId')?.value;

            if (!currentUserId) {
                alert('Vui lòng đăng nhập để thêm sản phẩm vào giỏ hàng');
                window.location.href = '/login';
                return;
            }

            if (!productId) {
                alert('Không tìm thấy thông tin sản phẩm');
                return;
            }

            this.disabled = true;
            const originalText = this.textContent;
            this.textContent = 'Đang thêm...';

            const formData = new URLSearchParams();
            formData.append('userId', currentUserId);
            formData.append('productId', productId);
            formData.append('quantity', '1');
            formData.append('_csrf', document.getElementById('csrfToken')?.value);

            fetch('/api/cart/add', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': document.getElementById('csrfToken')?.value
                },
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => Promise.reject(text));
                    }
                    return response.text();
                })
                .then(msg => {
                    alert(msg || 'Đã thêm sản phẩm vào giỏ hàng');
                    location.reload(); // Làm mới trang để cập nhật giỏ hàng
                })
                .catch(err => {
                    console.error('Add to cart error:', err);
                    alert('Có lỗi xảy ra: ' + err);
                })
                .finally(() => {
                    this.disabled = false;
                    this.textContent = originalText;
                });
        });
    });

    // AJAX cập nhật số lượng
    document.querySelectorAll('.btn-update-qty').forEach(button => {
        button.addEventListener('click', function () {
            const itemId = this.dataset.itemId;
            const action = this.dataset.action;
            const quantityField = document.getElementById('qty-' + itemId);
            const checkbox = document.getElementById('item-' + itemId);

            if (!itemId || !quantityField || !checkbox || !csrfToken) {
                console.error('Missing elements or CSRF token for item:', itemId);
                return;
            }

            this.disabled = true;

            const formData = new URLSearchParams();
            formData.append('itemId', itemId);
            formData.append('action', action);
            formData.append('_csrf', csrfToken);

            fetch('/api/cart/update', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-CSRF-TOKEN': csrfToken
                },
                body: formData
            })
                .then(response => {
                    if (!response.ok) {
                        return response.text().then(text => Promise.reject(text));
                    }
                    return response.text();
                })
                .then(msg => {
                    let qty = parseInt(quantityField.value) || 0;
                    qty += action === 'increase' ? 1 : -1;

                    if (qty <= 0) {
                        const card = document.getElementById('card-' + itemId);
                        if (card) card.remove();
                    } else {
                        quantityField.value = qty;
                        checkbox.dataset.quantity = qty;
                    }
                    updateTotals();
                })
                .catch(err => {
                    console.error('Update quantity error:', err);
                    alert('Có lỗi xảy ra khi cập nhật: ' + err);
                })
                .finally(() => {
                    this.disabled = false;
                });
        });
    });
    // Khởi tạo
    updateTotals();
});