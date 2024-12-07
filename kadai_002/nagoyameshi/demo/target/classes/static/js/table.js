const tbody = document.querySelector('tbody');
const rows = tbody.querySelectorAll('tr');

rows.forEach((row) => {
  //マウスオーバーイベント
  row.addEventListener('mouseenter', () => {
    hoverEffect(row, true);
  });
  row.addEventListener('mouseout', () => {
    hoverEffect(row, false);
  });

  //クリックイベント
  row.addEventListener('click', () => {
    window.location.href = row?.dataset?.href;
  });
});

function hoverEffect(row, isHovered) {
  const tds = row.querySelectorAll('td');
  tds.forEach((td) => linkAppearance(td, isHovered));
}

function linkAppearance(td, isHovered) {
  if (isHovered) {
    td.classList.add('text-primary');
    td.classList.add('text-decoration-underline');
  } else {
    td.classList.remove('text-primary');
    td.classList.remove('text-decoration-underline');
  }
}
