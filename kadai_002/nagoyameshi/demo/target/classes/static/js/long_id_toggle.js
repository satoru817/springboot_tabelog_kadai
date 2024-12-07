const longIds = document.querySelectorAll('.long_id');

longIds.forEach((span) => {
  const parent = span.closest('li') || span.closest('tr');
  parent.addEventListener('dblclick', () => {
    toggleDisplay(span);
  });
});

function toggleDisplay(elem) {
  const fullId = elem.dataset.fullId;
  const shortId = elem.dataset.fullId.substring(0, 8) + '...';

  if (elem.textContent === shortId) {
    elem.textContent = fullId;
  } else {
    elem.textContent = shortId;
  }
}
