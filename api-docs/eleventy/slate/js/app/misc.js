;(function () {
  'use strict';

  // click to show hide response examples
  $(function() {
    // $("a.responsesShow").on("click", function() {
    //   $( "a.responsesShow").hide();
    //   $( "a.responsesHide, .response-examples" ).show();
    //   return false;
    // });
    $("span.responsesToggle, span.bodyToggle").on("click", function(e) {
      e.stopPropagation();
      var target = "#" + $(this).data("target");
      var newText = $(this).text()=='show'? 'hide' : 'show';
      $(this).html(newText);
      $(target).toggle();
    });
  });
})();
