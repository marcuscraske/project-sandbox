<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--
    Copyright © Marcus Craske <c:out value="${copyright_year}"/>
-->
<!DOCTYPE html>
<html>
	<head>
		<title>Project Sandbox - Prototype</title>

		<!-- Third-Party -->
		<script src="/content/js/third-party/gl-matrix-min.js"></script>

		<!-- Main -->
		<script src="/content/js/project-sandbox.js"></script>

		<!-- Engine -->
		<script src="/content/js/engine/_prototype_inheritence.js"></script>
		<script src="/content/js/engine/asset-loader.js"></script>
		<script src="/content/js/engine/buffer-cache.js"></script>
		<script src="/content/js/engine/camera.js"></script>
		<script src="/content/js/engine/shaders.js"></script>
		<script src="/content/js/engine/primitive.js"></script>
		<script src="/content/js/engine/primitive-bar.js"></script>
		<script src="/content/js/engine/entity.js"></script>
		<script src="/content/js/engine/effect.js"></script>
		<script src="/content/js/engine/trail.js"></script>
		<script src="/content/js/engine/texturesrc.js"></script>
		<script src="/content/js/engine/texture.js"></script>
		<script src="/content/js/engine/textures.js"></script>
		<script src="/content/js/engine/map.js"></script>
		<script src="/content/js/engine/keyboard.js"></script>
		<script src="/content/js/engine/mouse.js"></script>
		<script src="/content/js/engine/comms.js"></script>
		<script src="/content/js/engine/comms-packet.js"></script>
		<script src="/content/js/engine/utils.js"></script>

		<!-- Game -->
		<script src="/content/game/js/_namespace.js"></script>
		<script src="/content/game/js/ui.js"></script>

		<script src="/content/game/js/player.js"></script>
		<script src="/content/game/js/ice-cream-van.js"></script>

	</head>
	<body>

        <c:if test="${content_header}">
            <h2>
                <c:out value="${title}"/>
            </h2>
        </c:if>

        <tiles:insertAttribute name="content" />

	</body>
</html>