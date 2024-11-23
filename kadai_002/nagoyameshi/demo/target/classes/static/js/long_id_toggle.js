const longIds = document.querySelectorAll('.long_id');

longIds.forEach(span=>{
    const li = span.closest('li');
    li.addEventListener('dblclick',()=>{
        toggleDisplay(span);
    });
});


function toggleDisplay(elem){
    const fullId = elem.dataset.fullId;
    const shortId = elem.dataset.fullId.substring(0,8)+'...';

    if(elem.textContent === shortId){
        elem.textContent = fullId;
    }else{
        elem.textContent = shortId;
    }
}