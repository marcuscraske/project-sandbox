<%@ taglib prefix="c"		uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form"    uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ps"      tagdir="/WEB-INF/tags/main" %>

<c:if test="not empty csrf">
    <p class="error">
        An error occurred, most likely from session timeout or duplicate tabs, please try again...
    </p>
</c:if>

<ps:authenticated auth="false">
    <form:form cssClass="box guest" method="post" action="/auth/guest" modelAttribute="guestForm">
        <h3>
            Play as Guest...
        </h3>
        <table>
            <tr>
                <td>
                    <ps:inputWithErrors modelAttribute="guestForm" path="nickname" placeholder="Enter a nickname..." />
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" value="Join" class="button" />
                </td>
            </tr>
        </table>

        <ps:csrf />
        <ps:errorList modelAttribute="guestForm" cssClass="error" />

    </form:form>

    <form:form cssClass="box login" method="post" action="/auth/login" modelAttribute="loginForm">
        <h3>
            Login
        </h3>
        <table>
            <tr>
                <td>
                    <ps:inputWithErrors modelAttribute="loginForm" path="nickname" placeholder="Nickname..." />
                </td>
            </tr>
            <tr>
                <td>
                    <ps:inputWithErrors modelAttribute="loginForm" path="password" placeholder="Password..." type="password" />
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" value="Login" class="button" />
                </td>
            </tr>
        </table>

        <ps:csrf />
        <ps:errorList modelAttribute="loginForm" cssClass="error" singleError="true" />

    </form:form>

    <form:form cssClass="box register" method="post" action="/auth/register" modelAttribute="registerForm">
        <h3>
            Register
        </h3>
        <table>
            <tr>
                <td>
                    <ps:inputWithErrors modelAttribute="registerForm" path="nickname" placeholder="Enter a nickname..." />
                </td>
            </tr>
            <tr>
                <td>
                    <ps:inputWithErrors modelAttribute="registerForm" path="password" placeholder="Enter a password..." type="password" />
                </td>
            </tr>
            <tr>
                <td>
                    <ps:inputWithErrors modelAttribute="registerForm" path="email" placeholder="E-mail..." />
                </td>
            </tr>
            <tr>
                <td>
                    <input type="submit" value="Login" class="button" />
                </td>
            </tr>
        </table>

        <ps:csrf />
        <ps:errorList modelAttribute="registerForm" cssClass="error" />

    </form:form>
</ps:authenticated>

<ps:authenticated auth="true">
    <form class="box user" method="post" action="/auth/user">
        <h3>
            Welcome <c:out value="${user.nickname}" />!
        </h3>
        <table>
            <tr>
                <td>
                    <input type="submit" value="Join Now" class="button" />
                </td>
            </tr>
        </table>
        <ps:csrf />
    </form>
</ps:authenticated>

<div class="online">
    <span><c:out value="${playersOnline}" /></span> players online
</div>
