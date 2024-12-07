const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('[id^="doMask_"]').forEach((button) => {
    button.addEventListener('click', function () {
      doMask(this);
    });
  });

  document.querySelectorAll('[id^="doUnMask_"]').forEach((button) => {
    button.addEventListener('click', function () {
      doUnMask(this);
    });
  });
});
//レビュー公開化メソッド
async function doUnMask(btn) {
  const reviewId = btn.id.split('_')[1];

  try {
    const response = await fetch(`/admin/review/unmask/${reviewId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        [csrfHeader]: csrfToken,
      },
    });

    if (response.ok) {
      alert('レビューを公開しました');

      const modal = bootstrap.Modal.getInstance(document.getElementById(`review_unmask_${reviewId}`));
      modal.hide();

      location.reload();
    } else {
      const errorData = await response.text();
      alert(`エラーが発生しました:${errorData}`);
    }
  } catch (error) {
    console.error('error:', error);
    alert('エラーが発生しました');
  }
}
//レビュー非公開化メソッド
async function doMask(btn) {
  const reviewId = btn.id.split('_')[1];

  const reasonText = document.getElementById(`maskingReason_${reviewId}`).value;

  try {
    const response = await fetch(`/admin/review/hide/${reviewId}?reason=${encodeURIComponent(reasonText)}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        [csrfHeader]: csrfToken,
      },
    });

    if (response.ok) {
      alert('レビューを非公開にしました');

      const modal = bootstrap.Modal.getInstance(document.getElementById(`review_mask_${reviewId}`));
      modal.hide();

      location.reload();
    } else {
      const errorData = await response.text();
      alert(`エラーが発生しました: ${errorData}`);
    }
  } catch (error) {
    console.error('error:', error);
    alert('エラーが発生しました');
  }
}
