document.addEventListener('DOMContentLoaded', function () {
  if (monthlySummary && monthlySummary.length > 0) {
    const labels = monthlySummary.map((data) => {
      const [year, month] = data.yearMonth.split('-');
      return `${year}年${parseInt(month)}月`;
    });

    new Chart(document.getElementById('monthlyTrend'), {
      type: 'bar',
      data: {
        labels: labels,
        datasets: [
          {
            type: 'bar',
            label: 'アクティブユーザー数',
            data: monthlySummary.map((data) => data.numberOfPayments),
            backgroundColor: 'rgba(75, 192, 192, 0.5)',
            yAxisID: 'y',
          },
          {
            type: 'bar',
            label: '累計有効ユーザー数',
            data: monthlySummary.map((data) => data.totalEffectiveUser),
            backgroundColor: 'rgba(54, 162, 235, 0.5)',
            yAxisID: 'y',
          },
          {
            type: 'line',
            label: '売上',
            data: monthlySummary.map((data) => data.numberOfPayments * 300),
            borderColor: 'rgb(255, 99, 132)',
            yAxisID: 'y1',
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          title: {
            display: true,
            text: '月別ユーザー数と売上推移',
          },
        },
        scales: {
          y: {
            type: 'linear',
            position: 'left',
            stacked: false,
            title: {
              display: true,
              text: 'ユーザー数',
            },
          },
          y1: {
            type: 'linear',
            position: 'right',
            title: {
              display: true,
              text: '売上（円）',
            },
            ticks: {
              callback: function (value) {
                return value.toLocaleString() + '円';
              },
            },
            grid: {
              drawOnChartArea: false,
            },
          },
        },
      },
    });
  } else {
    document.getElementById('monthlyTrend').innerHTML =
      '<div class="text-center text-muted mt-3">データがありません</div>';
  }
});
