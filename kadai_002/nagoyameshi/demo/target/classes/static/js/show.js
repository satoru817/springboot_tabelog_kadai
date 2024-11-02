$('.carousel').slick({
    autoplay:true,
    dots:true,
    infinite:true,
    autoplaySpeed:5000,
    arrows:true,
    centerMode:true,
    variableWidth:true,
    appendArrows: $('.arrow_box'),
    prevArrow: '<div class="slide-arrow prev-arrow"></div>',
    nextArrow: '<div class="slide-arrow next-arrow"></div>'
});