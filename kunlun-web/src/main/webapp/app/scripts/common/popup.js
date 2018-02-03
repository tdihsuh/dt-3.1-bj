var hsPopup = {
	$: function (ele) {
		if (typeof (ele) == "object")
			return ele;
		else if (typeof (ele) == "string" || typeof (ele) == "number")
			return document.getElementById(ele.toString());
		return null;
	},
	mousePos: function (e) {
		var x, y;
		var e = e || window.event;
		return {
			x: e.clientX + document.body.scrollLeft + document.documentElement.scrollLeft,
			y: e.clientY + document.body.scrollTop + document.documentElement.scrollTop
		};
	},
	start: function (obj,txt) {
		var self = this;
		var t = self.$("popup");
		obj.mousemove(function (e) {
			var mouse = self.mousePos(e);
			t.style.left = mouse.x + 10 + 'px';
			t.style.top = mouse.y + 10 + 'px';
			t.innerHTML = txt;
			t.style.display = '';
		});
		obj.mouseout(function () {
			t.style.display = 'none';
		});
	}
}