<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<title>Visualização Ficha de Atendimento</title>
	</head>
	<body>
		<form action="FichaDigitalRenderServlet/" method="post">
			<table>
			    <%--
				<tr>
					<td>Usuário: </td>
					<td><input type="text" name="username"/></td>
				</tr>
				<input type="hidden" name="action" value="sendXML"/>
				<tr>
					<td>Senha: </td>
					<td><input type="password" name="password"/></td>
				</tr>
				--%>
				<tr>
					<td>Id: </td>
					<td> <input type="text" name="idFormcopies" /></td>
				</tr>
				<tr>
					<td></td>
					<td align="right"><input type="submit" name="Submit" value="Visualizar" /></td>
				</tr>
			</table>
		</form>
	</body>
</html>