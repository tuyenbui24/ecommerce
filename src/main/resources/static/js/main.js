document.addEventListener("DOMContentLoaded", function () {
    // Hiện menu mobile
    const menuButton = document.querySelector("#menu-button");
    const menuList = document.querySelector("#menu-list");

    if (menuButton && menuList) {
        menuButton.addEventListener("click", function () {
            menuList.classList.toggle("show-menu");
            menuList.classList.toggle("d-none");
        });
    }

    // Bootstrap tooltip
    try {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (el) {
            return new bootstrap.Tooltip(el);
        });
    } catch (e) {
        console.log("Tooltip không được khởi tạo", e);
    }

    // Dropdown hover effect (desktop only)
    function addDropdownHoverEffect() {
        const dropdowns = document.querySelectorAll(".dropdown");
        dropdowns.forEach(function (dropdown) {
            const menu = dropdown.querySelector(".dropdown-menu");
            if (!menu) return;

            dropdown.addEventListener("mouseenter", function () {
                if (window.innerWidth >= 992) {
                    menu.classList.add("show");
                }
            });
            dropdown.addEventListener("mouseleave", function () {
                if (window.innerWidth >= 992) {
                    menu.classList.remove("show");
                }
            });
        });
    }

    // Dropdown tùy chỉnh (ví dụ dropdown theo loại sản phẩm)
    // const namDropdown = document.querySelector("#nam-dropdown");
    // const namDropdownMenu = document.querySelector("#nam-dropdown-menu");
    //
    // if (namDropdown && namDropdownMenu) {
    //     namDropdown.addEventListener("click", function (event) {
    //         event.preventDefault();
    //         event.stopPropagation();
    //         namDropdownMenu.classList.toggle("show");
    //     });
    //
    //     document.addEventListener("click", function (event) {
    //         if (!namDropdown.contains(event.target)) {
    //             namDropdownMenu.classList.remove("show");
    //         }
    //     });
    // }

    // Toggle hiện/ẩn mật khẩu
    document.addEventListener('click', function (e) {
        if (!e.target.closest('.toggle-pass')) return;
        const btn = e.target.closest('.toggle-pass');
        const input = document.querySelector(btn.dataset.target);
        if (!input) return;

        const icon = btn.querySelector('i');
        const showing = input.type === 'text';
        input.type = showing ? 'password' : 'text';
        icon.classList.toggle('fa-eye', showing);
        icon.classList.toggle('fa-eye-slash', !showing);
    });

    // Kích hoạt dropdown hover ngay từ đầu
    addDropdownHoverEffect();
    window.addEventListener("resize", addDropdownHoverEffect);
});
