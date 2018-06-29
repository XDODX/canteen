/**
 * Created by PENGYU on 18/5/8.
 */
;(function (exports) {
    var KeyBoard = function (input, options, orderId) {
        var body = document.getElementsByTagName('body')[0];
        var DIV_ID = options && options.divId || '__w_l_h_v_c_z_e_r_o_divid';

        if (document.getElementById(DIV_ID)) {
            body.removeChild(document.getElementById(DIV_ID));
        }


        this.input = input;
        this.el = document.createElement('div');

        var self = this;
        var zIndex = options && options.zIndex || 9999;
        var width = options && options.width || '450px';
        var height = options && options.height || '690px';
        var fontSize = options && options.fontSize || '15px';
        var backgroundColor = options && options.backgroundColor || '#fff';
        var TABLE_ID = options && options.table_id || 'table_0909099';
        var mobile = typeof orientation !== 'undefined';

        this.el.id = DIV_ID;
        this.el.style.position = 'fixed';
        //this.el.style.left = 0;
        // this.el.style.right = '20px';
        // this.el.style.bottom = '100px';
        // this.el.style.left='calc( 50% - 150px )';
        // this.el.style.top='calc( 50% - 155px )';
        this.el.style.left = '50%';
        this.el.style.top = '50%';
        this.el.style.transform = ' translate(-50%,-50%)';
        this.el.style.zIndex = zIndex;
        this.el.style.width = width;
        this.el.style.height = height;
        this.el.style.backgroundColor = backgroundColor;

        //样式
        var cssStr = '<style type="text/css">';
        cssStr += '#' + TABLE_ID + '{text-align:center;width:100%;height:160px;border-top:1px solid #CECDCE;background-color:#FFF;}';
        cssStr += '#' + TABLE_ID + ' td{width:33%; height:150px;font-size:70px; border:1px solid #ddd;border-right:0;border-top:0;}';
        if (!mobile) {
            cssStr += '#' + TABLE_ID + ' td:hover{background-color:#1FB9FF;color:#FFF;}';
        }
        cssStr += '</style>';

        var inu = '<input type="text" style="height: 70px;width:180px;margin: 10px 0;font-size:70px;" readonly id="val">';


        //Button
        var btnStr = '<div style="width:140px;height:70px;line-height:70px;margin-top:10px; background-color:#1FB9FF;';
        btnStr += 'float:right;margin-right:5px;text-align:center;color:#fff;';
        btnStr += 'border-radius:3px;margin-bottom:5px;cursor:pointer; font-size:60px">删除</div>';

        //table
        var tableStr = '<table id="' + TABLE_ID + '" border="0" cellspacing="0" cellpadding="0">';
        tableStr += '<tr><td>1</td><td>2</td><td>3</td></tr>';
        tableStr += '<tr><td>4</td><td>5</td><td>6</td></tr>';
        tableStr += '<tr><td>7</td><td>8</td><td>9</td></tr>';
        tableStr += '<tr><td style="background-color:#D3D9DF;">取消</td><td>0</td>';
        tableStr += '<td style="background-color:#D3D9DF;">完成</td></tr>';
        tableStr += '</table>';
        this.el.innerHTML = cssStr + inu + btnStr + tableStr;

        self.input.value = '';
        $('#val').val('');

        function addEvent(e) {
            var ev = e || window.event;
            var clickEl = ev.element || ev.target;
            var value = clickEl.textContent || clickEl.innerText;
            if (clickEl.tagName.toLocaleLowerCase() === 'td' && value !== "完成" && value !== "取消") {
                if (self.input) {
                    self.input.value += value;

                    document.getElementById('val').value += value;
                }
            } else if (clickEl.tagName.toLocaleLowerCase() === 'div' && value === "删除") {

                var num = self.input.value;
                if (num) {
                    var newNum = num.substr(0, num.length - 1);
                    self.input.value = newNum;
                    document.getElementById('val').value = newNum;
                }
            } else if (clickEl.tagName.toLocaleLowerCase() === 'td' && value === "完成") {
                if ($('#val').val() > 50) {
                    alert('叫号器最大限制50');
                    return;
                }
                submitWait(orderId, $('#val').val());
                init();//
            }
            else if (clickEl.tagName.toLocaleLowerCase() === 'td' && value === "取消") {

                body.removeChild(document.getElementById(DIV_ID));
            }
        }

        if (mobile) {
            this.el.ontouchstart = addEvent;
        } else {
            this.el.onclick = addEvent;
        }
        body.appendChild(this.el);
    }

    exports.KeyBoard = KeyBoard;

})(window);