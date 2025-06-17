document.addEventListener("DOMContentLoaded", function () {
    // Thêm sản phẩm
    document.querySelectorAll(".add-to-cart-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const productId = this.dataset.id;

            fetch("/cart/add", {
                method: "POST",
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: "productId=" + productId
            }).then(res => {
                if (res.ok) alert("✅ Đã thêm vào giỏ hàng");
            });
        });
    });

    // Cập nhật số lượng
    document.querySelectorAll(".qty-input").forEach(input => {
        input.addEventListener("change", function () {
            const itemId = this.dataset.id;
            const quantity = this.value;

            fetch("/cart/update", {
                method: "POST",
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: "itemId=" + itemId + "&quantity=" + quantity
            }).then(() => location.reload());
        });
    });

    // Xóa sản phẩm
    document.querySelectorAll(".remove-item-btn").forEach(btn => {
        btn.addEventListener("click", function () {
            const itemId = this.dataset.id;

            fetch("/cart/remove?itemId=" + itemId, {
                method: "DELETE"
            }).then(() => location.reload());
        });
    });
});
