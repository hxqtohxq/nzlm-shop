
$(function () {

    console.log(window.location.href.substr(window.location.href.lastIndexOf("?")+1));
    tmptype = window.location.href.substr(window.location.href.lastIndexOf("?")+1);

    var attributename = tmptype.substr(tmptype.lastIndexOf("=")+1);
    var attributevalue = tmptype.substr(0,tmptype.lastIndexOf("="));
    console.log(attributename);
    console.log(attributevalue);

    //下面是初始化的商品数据
    var goodslist_htmlobj = $.ajax({url: "queryKeyWord.txt", async: false});
//    var goodslist_htmlobj = $.ajax({url: "localhost:8080/shoppingsolr2/solr_queryByType.action?"+tmptype, async: false});

    var attributes_htmlobj = $.ajax({url: "getAttribute.txt", async: false});
//    var attributes_htmlobj = $.ajax({url: "http://localhost:8080/shoppingsolr2/solr_getAttrbutes.action?"+tmptype, async: false});

    var attributes = eval('(' + attributes_htmlobj.responseText + ")");

    var keyWordGoods = eval(goodslist_htmlobj.responseText);

    var postModel = [];

    postModel.push({ name : attributename, value : attributevalue});

    $.each(attributes, function (keyname, data) {
        var myul = $("<ul></ul>");
        $.each(data, function (i, data2) {
            myul.append($("<li></li>").html(data2).on('click', function () {
                if ($(this).hasClass("press")) {
                    $(this).removeAttr("class");
                } else {
                    $(this).siblings().removeAttr("class");
                    $(this).attr("class", "press");
                }

                $(".classification_attr .press").parent().siblings().each(function (i, data2) {
                    var mydata = $(data2).html() + '_' + $(data2).siblings().find(".press").html();
                    postModel.push({ name : 'goodsAtributes', value : mydata});
                });
                //console.log(postModel);
//                
//                $.post('localhost:8080/shoppingsolr2/goods_queryByAttributes.action',postModel,function(data){
                $.post('queryKeyWord.txt',postModel,function(data){
                    console.log(eval(data));
                    keyWordGoods = eval(data);
                    $(".main .container .goods_list .goods_show ul").empty();
                    $.each(keyWordGoods, function (i, data) {
                        var goods = $("<li></li>").append(
                            $("<div class='good_img'></div>").append(
                                $("<img />").attr("src", "img/Q5423EFZ41_CGR0_N01.JPG"))).append(
                            $("<a class='name'></a>").attr("href", data.goodspic).html(data.goodsname)).append(
                            $("<span class='price'></span>").text(data.goodsprice)).append(
                            $("<em></em>").append($("<img />").attr("src", "img/icon_cart.png/")).html("1000"));
                        $(".goods_show > ul").append(goods);
                    });
                });
            }));
        });

        $(".classification").append(
            $("<div></div>").attr("class", "classification_attr").append(
                $("<div></div>").attr("class", "attrname").html(keyname)).append(
                myul));

    });

    $.each(keyWordGoods, function (i, data) {
        var goods = $("<li></li>").append(
            $("<div class='good_img'></div>").append(
                $("<img />").attr("src", "img/Q5423EFZ41_CGR0_N01.JPG"))).append(
            $("<a class='name'></a>").attr("href", data.goodspic).html(data.goodsname)).append(
            $("<span class='price'></span>").text(data.goodsprice)).append(
            $("<em></em>").append($("<img />").attr("src", "img/icon_cart.png/")).html("1000"));
        $(".goods_show > ul").append(goods);
    });

    $('.class_selection ul li').bind('click', function () {
        var $this = $(this);
        $this.addClass('focus').siblings().removeClass('focus');
    });

    $('.good_img img').bind({
        'mouseleave': function () {
            var $this = $(this);
            $this.addClass('imgdown').siblings().removeClass('imgdown');
        }
    })
});