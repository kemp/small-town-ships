<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="me.smalltownships.Product" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html lang="en">
    <jsp:include page="_head.jsp">
        <jsp:param name="title" value="Products"/>
    </jsp:include>
    
    <body class="bg-light">
    
        <jsp:include page="_nav.jsp"></jsp:include>

        <main class="container mt-3">
            <h1>Products</h1>
            	
            <div class="card-columns">
                <c:forEach items="${products}" var="product">
                    <div class="card mx-auto mb-2">
                        <a href="./product?id=${product.id}"><img src="${ product.image }" class="card-img-top" alt="${ product.name }"></a>
                        <div class="card-body">
                            <h5 class="card-title">${ product.name }</h5>
                            <p class="card-text">Price: ${ product.getFormattedPrice() }</p>
                            <a href="./product?id=${product.id}" class="btn btn-info">Open</a>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </main>
        
        <jsp:include page="_footer.jsp"></jsp:include>
    </body>
</html>
