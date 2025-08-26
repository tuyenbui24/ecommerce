document.addEventListener("DOMContentLoaded", async () => {
    const staffId = getUrlParam("id");
    const formTitle = document.getElementById("form-title");
    const staffForm = document.getElementById("staffForm");
    const errorBox = document.getElementById("errorBox");
    const rolesContainer = document.getElementById("rolesContainer");
    const photoPreview = document.getElementById("photoPreview");
    const photoPreviewContainer = document.getElementById("photoPreviewContainer");

    // Load header
    loadHeader();

    // Load roles
    await loadRoles();

    // If editing, load staff info
    if (staffId) {
        formTitle.textContent = "Chỉnh sửa nhân viên";
        loadStaff(staffId);
    }

    // Form submit
    staffForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        await submitForm(staffId);
    });

    // Preview image
    document.getElementById("imageFile").addEventListener("change", function () {
        const file = this.files[0];
        if (file) {
            photoPreviewContainer.classList.remove("d-none");
            photoPreview.src = URL.createObjectURL(file);
        }
    });

    // --- FUNCTIONS ---

    function getUrlParam(name) {
        const url = new URL(window.location.href);
        return url.searchParams.get(name);
    }

    function loadHeader() {
        fetch("/fragments/header.html")
            .then(res => res.text())
            .then(html => document.getElementById("header-container").innerHTML = html);
    }

    async function loadRoles() {
        try {
            const res = await fetch("/api/staffs/roles");
            const roles = await res.json();

            roles.forEach(role => {
                const col = document.createElement("div");
                col.className = "col-6";

                const checkDiv = document.createElement("div");
                checkDiv.className = "form-check";

                const checkbox = document.createElement("input");
                checkbox.className = "form-check-input";
                checkbox.type = "checkbox";
                checkbox.value = role.id;
                checkbox.id = `role_${role.id}`;
                checkbox.name = "roleIds";

                const label = document.createElement("label");
                label.className = "form-check-label";
                label.setAttribute("for", `role_${role.id}`);
                label.textContent = role.name;

                checkDiv.appendChild(checkbox);
                checkDiv.appendChild(label);
                col.appendChild(checkDiv);
                rolesContainer.appendChild(col);
            });
        } catch (e) {
            console.error("Lỗi khi load vai trò:", e);
        }
    }

    async function loadStaff(id) {
        try {
            const res = await fetch(`/api/staffs/${id}`);
            if (!res.ok) throw new Error("Không tìm thấy nhân viên");
            const staff = await res.json();

            document.getElementById("staffId").value = staff.id;
            document.getElementById("lastName").value = staff.lastName;
            document.getElementById("firstName").value = staff.firstName;
            document.getElementById("email").value = staff.email;
            document.getElementById("enabled").checked = staff.enabled;

            if (staff.photos) {
                photoPreview.src = `/staff-photos/${staff.id}/${staff.photos}`;
                photoPreviewContainer.classList.remove("d-none");
            }

            // Check roles
            const roleCheckboxes = document.querySelectorAll("input[name='roleIds']");
            roleCheckboxes.forEach(cb => {
                if (staff.roles.some(r => r.id == cb.value)) {
                    cb.checked = true;
                }
            });
        } catch (err) {
            console.error(err);
            errorBox.textContent = err.message;
            errorBox.classList.remove("d-none");
        }
    }

    async function submitForm(id) {
        try {
            const formData = new FormData();
            formData.append("lastName", document.getElementById("lastName").value);
            formData.append("firstName", document.getElementById("firstName").value);
            formData.append("email", document.getElementById("email").value);

            const password = document.getElementById("password").value;
            if (password) {
                formData.append("password", password);
            }

            formData.append("enabled", document.getElementById("enabled").checked);

            const imageFile = document.getElementById("imageFile").files[0];
            if (imageFile) {
                formData.append("imageFile", imageFile);
            }

            const roleCheckboxes = document.querySelectorAll("input[name='roleIds']:checked");
            roleCheckboxes.forEach(cb => formData.append("roleIds", cb.value));

            const url = id ? `/api/staffs/${id}` : "/api/staffs";
            const method = id ? "PUT" : "POST";

            const res = await fetch(url, {
                method: method,
                body: formData,
            });

            if (res.ok) {
                window.location.href = "/staff"; // Chuyển về trang danh sách
            } else {
                const errorText = await res.text();
                errorBox.textContent = `Lỗi: ${errorText}`;
                errorBox.classList.remove("d-none");
            }
        } catch (err) {
            console.error("Lỗi khi gửi form:", err);
            errorBox.textContent = err.message;
            errorBox.classList.remove("d-none");
        }
    }
});
