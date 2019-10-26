<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="java.util.Date" import="java.text.SimpleDateFormat" %>
<!doctype html>
<html lang="en">
    <jsp:include page="_head.jsp">
        <jsp:param name="title" value="Order Confirmed"/>
    </jsp:include>
    
    <body class="bg-light">
    
        <jsp:include page="_nav.jsp"></jsp:include>

        <main class="container mt-3 text-center">
            <h1 class="mr-auto">Your order has been placed! 🎉</h1>
            
            <div class="row">
                <div class="col-md-6 offset-md-3">
                    <img class="img-fluid d-block mx-auto" src="http://img.kpopmap.com/wp-content/uploads_kpopmap/2017/01/nyangtalk-emoticon-celebration.gif" alt="" />
                </div>
            </div>
            
            
            <p>It should arrive on <%= (new SimpleDateFormat("EEE, MMMM d, yyyy")).format(new Date((new Date()).getTime() + 4*24*60*60*1000)) %>.</p>
        </main>
        
        <jsp:include page="_footer.jsp"></jsp:include>
        
        <script src="cart.js"></script>
        
        <script>
        	clearCart();
        </script>
    </body>
</html>
