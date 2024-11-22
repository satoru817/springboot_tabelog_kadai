document.addEventListener('DOMContentLoaded', function() {
    const submenus = document.querySelectorAll('.dropdown_submenu');

    submenus.forEach(submenu => {
        // クリックイベント（モバイル用）
        submenu.addEventListener('click', function(e) {
            if (!submenu.classList.contains('show')) {
                e.preventDefault();
                e.stopPropagation();

                // 他のサブメニューを閉じる
                closeOtherSubmenus(submenu);

                // このサブメニューを開く
                openSubmenu(submenu);
            }
        });

        // マウスオーバーイベント
        submenu.addEventListener('mouseenter', function(e) {
            closeOtherSubmenus(submenu);
            openSubmenu(submenu);
        });

        // マウスアウトイベント
        submenu.addEventListener('mouseleave', function(e) {
            // マウスが移動した先の要素をチェック
            const relatedTarget = e.relatedTarget;
            // サブメニュー内に移動した場合は閉じない
            if (!submenu.contains(relatedTarget)) {
                closeSubmenu(submenu);
            }
        });

        // 親メニューが閉じられたときの処理
        submenu.closest('.dropdown-menu').addEventListener('hidden.bs.dropdown', function() {
            closeSubmenu(submenu);
        });
    });

    // 補助関数：サブメニューを開く
    function openSubmenu(submenu) {
        submenu.classList.add('show');
        const dropdownMenu = submenu.querySelector('.dropdown-menu');
        if (dropdownMenu) {
            dropdownMenu.classList.add('show');
            adjustPosition(dropdownMenu);
        }
    }

    // 補助関数：サブメニューを閉じる
    function closeSubmenu(submenu) {
        submenu.classList.remove('show');
        const dropdownMenu = submenu.querySelector('.dropdown-menu');
        if (dropdownMenu) {
            dropdownMenu.classList.remove('show');
        }
    }

    // 補助関数：他のサブメニューを閉じる
    function closeOtherSubmenus(currentSubmenu) {
        submenus.forEach(other => {
            if (other !== currentSubmenu && other.classList.contains('show')) {
                closeSubmenu(other);
            }
        });
    }

    // 補助関数：位置調整
    function adjustPosition(dropdownMenu) {
        const rect = dropdownMenu.getBoundingClientRect();
        if (rect.right > window.innerWidth) {
            dropdownMenu.style.left = 'auto';
            dropdownMenu.style.right = '100%';
        } else {
            dropdownMenu.style.left = '100%';
            dropdownMenu.style.right = 'auto';
        }
    }

    // ウィンドウのリサイズ時の処理
    window.addEventListener('resize', function() {
        submenus.forEach(submenu => {
            const dropdownMenu = submenu.querySelector('.dropdown-menu');
            if (dropdownMenu && dropdownMenu.classList.contains('show')) {
                adjustPosition(dropdownMenu);
            }
        });
    });
});