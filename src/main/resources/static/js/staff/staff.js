const apiUrl = '/api/staffs';
const photoBaseUrl = '/staff-photos/';

let currentPage = 1;
let totalPages = 1;
let currentKeyword = '';

document.addEventListener('DOMContentLoaded', function () {
    loadHeader();
    loadStaffs(currentPage);

    document.getElementById('searchForm').addEventListener('submit', function (e) {
        e.preventDefault();
        currentKeyword = document.getElementById('keyword').value.trim();
        loadStaffs(1);
    });
});

function loadHeader() {
    fetch('/fragments/header.html')  // ✅ Đường dẫn đúng với vị trí trong thư mục static
        .then(res => {
            if (!res.ok) {
                throw new Error('Không thể tải header.html');
            }
            return res.text();
        })
        .then(html => document.getElementById('header-container').innerHTML = html)
        .catch(err => {
            console.error(err);
            document.getElementById('header-container').innerHTML = '<div class="alert alert-danger">Không thể tải phần đầu trang</div>';
        });
}

function loadStaffs(page) {
    currentPage = page;

    const url = `${apiUrl}?page=${page}${currentKeyword ? '&keyword=' + encodeURIComponent(currentKeyword) : ''}`;

    fetch(url)
        .then(res => res.json())
        .then(data => {
            renderTable(data.data);
            renderPagination(data.currentPage, data.totalPages, data.totalItems);
        })
        .catch(err => console.error('Lỗi tải danh sách nhân viên:', err));
}

function renderTable(staffs) {
    const tbody = document.getElementById('staffTableBody');
    tbody.innerHTML = '';

    if (!staffs || staffs.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">Không có nhân viên nào.</td></tr>';
        return;
    }

    staffs.forEach(staff => {
        const tr = document.createElement('tr');

        const photoUrl = staff.photos ? `${photoBaseUrl}${staff.id}/${staff.photos}` : '/img/default-user.png';

        tr.innerHTML = `
      <td>${staff.id}</td>
      <td>${staff.email}</td>
      <td><img src="${photoUrl}" width="50" height="50" class="rounded-circle" alt="Ảnh nhân viên"></td>
      <td>${staff.lastName}</td>
      <td>${staff.firstName}</td>
      <td>${staff.role}</td>
      <td>
        <input type="checkbox" ${staff.enabled ? 'checked' : ''} onclick="toggleStatus(${staff.id}, this)">
      </td>
      <td>
        <a href="/staff/staff_form.html?id=${staff.id}" class="btn btn-sm btn-primary me-1">
          <i class="fas fa-pen"></i>
        </a>
        <button class="btn btn-sm btn-danger" onclick="deleteStaff(${staff.id})">
          <i class="fas fa-trash"></i>
        </button>
      </td>
    `;

        tbody.appendChild(tr);
    });
}

function renderPagination(page, total, totalElements) {
    totalPages = total;

    const info = document.getElementById('paginationInfo');
    info.innerHTML = `Trang ${page} / ${total} — Tổng ${totalElements} nhân viên`;

    const nav = document.getElementById('pagination');
    nav.innerHTML = '';

    for (let i = 1; i <= totalPages; i++) {
        const li = document.createElement('li');
        li.className = `page-item ${i === page ? 'active' : ''}`;
        li.innerHTML = `<button class="page-link" onclick="loadStaffs(${i})">${i}</button>`;
        nav.appendChild(li);
    }
}

function toggleStatus(id, checkbox) {
    const enabled = checkbox.checked;

    fetch(`${apiUrl}/${id}/status?enabled=${enabled}`, { method: 'PUT' })
        .then(res => {
            if (res.ok) {
                showMessage(`Đã ${enabled ? 'kích hoạt' : 'vô hiệu hóa'} nhân viên thành công`);
            } else {
                throw new Error('Thay đổi trạng thái thất bại');
            }
        })
        .catch(err => {
            console.error(err);
            checkbox.checked = !enabled;
            alert('Lỗi thay đổi trạng thái');
        });
}

function deleteStaff(id) {
    if (!confirm('Bạn có chắc muốn xóa nhân viên này?')) return;

    fetch(`${apiUrl}/${id}`, { method: 'DELETE' })
        .then(res => {
            if (res.ok) {
                showMessage('Xóa nhân viên thành công');
                loadStaffs(currentPage);
            } else {
                throw new Error('Xóa thất bại');
            }
        })
        .catch(err => {
            console.error(err);
            alert('Lỗi xóa nhân viên');
        });
}

function clearFilter() {
    document.getElementById('keyword').value = '';
    currentKeyword = '';
    loadStaffs(1);
}

function showMessage(msg) {
    const box = document.getElementById('messageBox');
    box.textContent = msg;
    box.classList.remove('d-none');

    setTimeout(() => box.classList.add('d-none'), 3000);
}
