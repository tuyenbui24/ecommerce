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

    const toastEl = document.getElementById("cart-toast");
    if (toastEl) {
        setTimeout(() => {
            toastEl.classList.remove("show");
            toastEl.classList.add("hide");
        }, 3000);
    }

    const form = document.querySelector("form");
    const newPassword = document.querySelector("#newPassword");
    const confirmPassword = document.querySelector("#confirmPassword");

    form.addEventListener("submit", (e) => {
        if (newPassword.value !== "" || confirmPassword.value !== "") {
            if (newPassword.value !== confirmPassword.value) {
                e.preventDefault();
                alert("Mật khẩu mới và nhập lại không khớp.");
            }
        }
    });

    let isDragging = false;
    let startX, scrollLeft;

    window.startDrag = function(e) {
        const el = e.currentTarget;
        isDragging = true;
        el.classList.add("dragging");
        startX = e.pageX - el.offsetLeft;
        scrollLeft = el.scrollLeft;
        e.preventDefault();
    }

    window.onDrag = function (e) {
        if (!isDragging) return;
        const el = e.currentTarget;
        e.preventDefault();
        const x = e.pageX - el.offsetLeft;
        const walk = (x - startX) * 1.5;
        el.scrollLeft = scrollLeft - walk;
    }

    window.stopDrag = function () {
        isDragging = false;
        document.querySelectorAll('.scroll-slider').forEach(el => el.classList.remove("dragging"));
    }

    window.scrollSlider = function (button, direction) {
        const slider = button.parentElement.querySelector(".scroll-slider");
        slider.scrollBy({ left: direction * 300, behavior: 'smooth' });

        // cập nhật mũi tên sau khi scroll
        setTimeout(() => toggleArrows(slider), 300);
    }


    function toggleArrows(slider) {
        const container = slider.parentElement;
        const leftArrow = container.querySelector(".slider-arrow:first-of-type");
        const rightArrow = container.querySelector(".slider-arrow:last-of-type");

        // Hiện mũi tên trái nếu có thể cuộn trái
        leftArrow.style.display = slider.scrollLeft > 0 ? 'block' : 'none';

        // Hiện mũi tên phải nếu còn nội dung phía phải
        const maxScrollLeft = slider.scrollWidth - slider.clientWidth - 5;
        rightArrow.style.display = slider.scrollLeft < maxScrollLeft ? 'block' : 'none';
    }

    // Gắn sự kiện cho slider
    document.querySelectorAll('.scroll-slider').forEach(slider => {
        slider.addEventListener('mousedown', startDrag);
        slider.addEventListener('scroll', () => toggleArrows(slider));
    });

    window.addEventListener('mousemove', onDrag);
    window.addEventListener('mouseup', stopDrag);


    window.addEventListener("load", () => {
        document.querySelectorAll('.scroll-slider').forEach(toggleArrows);
    });

    // Kích hoạt dropdown hover ngay từ đầu
    addDropdownHoverEffect();
    window.addEventListener("resize", addDropdownHoverEffect);
});
