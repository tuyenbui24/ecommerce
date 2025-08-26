document.addEventListener('DOMContentLoaded', function () {
    // Load danh mục sản phẩm
    fetch('/api/categories')
        .then(res => res.json())
        .then(categoryProducts => {
            for (const [categoryName, products] of Object.entries(categoryProducts)) {
                if (products.length > 0) {
                    const slug = products[0].categorySlug;
                    const li = `<li><a class="dropdown-item" href="/category/${slug}/page/1">${categoryName}</a></li>`;
                    const mobileLi = `<li class="nav-item"><a class="nav-link text-dark" href="/category/${slug}/page/1">${categoryName}</a></li>`;

                    if (slug.includes('nam')) {
                        $('#nam-menu-desktop').append(li);
                        $('#nam-menu-mobile').append(li); // nếu có menu mobile
                    } else if (slug.includes('nu')) {
                        $('#nu-menu-desktop').append(li);
                        $('#nu-menu-mobile').append(li); // nếu có menu mobile
                    }
                }
            }
        })
        .catch(() => {
            console.error('Không thể load danh mục sản phẩm');
        });

    // Load thông tin người dùng hiện tại
    fetch('/api/account/me')
        .then(res => res.json())
        .then(user => {
            // Hiển thị các mục ADMIN
            document.querySelectorAll('#menu-staff-desktop, #menu-user-desktop, #menu-product-desktop, #menu-order-desktop, #menu-staff-mobile, #menu-user-mobile, #menu-product-mobile, #menu-order-mobile')
                .forEach(el => el.classList.remove('d-none'));

            // Hiển thị icon giỏ hàng nếu là USER
            if (user.roles && user.roles.includes('USER')) {
                $('#cart-icon-desktop, #cart-icon-mobile').removeClass('d-none');
            }

            // Hiển thị thông tin đăng nhập
            const authHtml = `
                <div class="dropdown d-inline-block">
                    <a href="#" class="dropdown-toggle text-primary fw-bold text-decoration-none" id="userDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                        ${user.fullName}
                    </a>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="userDropdown">
                        <li><a class="dropdown-item" href="/profile">Hồ sơ</a></li>
                        <li><a class="dropdown-item" href="/orders">Đơn hàng</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item text-danger" href="#" id="logout-btn">Đăng xuất</a></li>
                    </ul>
                </div>
            `;
            $('#auth-section-desktop').html(authHtml);
            $('#mobile-user-link').attr('href', '/profile');

            // Đăng xuất
            $('#logout-btn').on('click', function (e) {
                e.preventDefault();
                $.ajax({
                    url: '/api/account/logout',
                    method: 'POST',
                    success: function () {
                        window.location.href = '/login';
                    },
                    error: function () {
                        alert('Lỗi khi đăng xuất');
                    }
                });
            });
        })
        .catch(() => {
            // Giao diện cho khách chưa đăng nhập
            const guestHtml = `
                <a href="/login" class="btn btn-outline-primary me-2">Đăng nhập</a>
                <a href="/register" class="btn btn-primary text-white">Đăng ký</a>
            `;
            $('#auth-section-desktop').html(guestHtml);
            $('#mobile-user-link').attr('href', '/login');
        });

    // Toggle menu mobile
    document.getElementById('menu-button')?.addEventListener('click', function () {
        $('#menu-list').toggleClass('d-none');
    });

    // Toggle search mobile
    document.querySelectorAll('.search-toggle').forEach(btn => {
        btn.addEventListener('click', function () {
            $('#search-form-mobile').toggleClass('d-none');
        });
    });
});
