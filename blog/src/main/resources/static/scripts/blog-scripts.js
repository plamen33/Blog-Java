$(function() {
    $('#messages li').click(function() {
        $(this).fadeOut();
    });
    setTimeout(function() {
        $('#messages li.info').fadeOut();
    }, 3000);
    setTimeout(function() {
        $('#messages li.error').fadeOut();
    }, 7000);
    setTimeout(function() {
        $('#messages li.warning').fadeOut();
    }, 5000);
});