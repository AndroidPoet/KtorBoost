import json
from aiohttp import web, WSMsgType


async def ws_handler(request: web.Request) -> web.WebSocketResponse:
    ws = web.WebSocketResponse()
    await ws.prepare(request)
    async for msg in ws:
        if msg.type == WSMsgType.TEXT:
            await ws.send_str(msg.data)
        elif msg.type == WSMsgType.ERROR:
            break
    return ws


async def socketio_handler(request: web.Request) -> web.WebSocketResponse:
    ws = web.WebSocketResponse()
    await ws.prepare(request)
    await ws.send_str('0{"sid":"abc","upgrades":[],"pingInterval":25000,"pingTimeout":20000}')
    async for msg in ws:
        if msg.type != WSMsgType.TEXT:
            continue
        text = msg.data
        if text.startswith("40"):
            await ws.send_str('42["message",{"id":21}]')
            break
    return ws


async def stomp_handler(request: web.Request) -> web.WebSocketResponse:
    ws = web.WebSocketResponse()
    await ws.prepare(request)
    async for msg in ws:
        if msg.type != WSMsgType.TEXT:
            continue
        text = msg.data
        if text.startswith("CONNECT"):
            await ws.send_str("CONNECTED\nversion:1.2\n\n\0")
        elif text.startswith("SUBSCRIBE"):
            await ws.send_str("MESSAGE\nsubscription:sub-0\n\n{\"id\":22}\0")
            break
    return ws


async def graphql_handler(request: web.Request) -> web.WebSocketResponse:
    ws = web.WebSocketResponse(protocols=("graphql-transport-ws",))
    await ws.prepare(request)
    async for msg in ws:
        if msg.type != WSMsgType.TEXT:
            continue
        packet = json.loads(msg.data)
        packet_type = packet.get("type")
        if packet_type == "connection_init":
            await ws.send_str(json.dumps({"type": "connection_ack"}))
        elif packet_type == "subscribe":
            await ws.send_str(
                json.dumps(
                    {
                        "id": packet.get("id", "1"),
                        "type": "next",
                        "payload": {"data": {"id": 23}},
                    }
                )
            )
            await ws.send_str(json.dumps({"type": "complete"}))
            break
    return ws


async def reverb_handler(request: web.Request) -> web.WebSocketResponse:
    ws = web.WebSocketResponse()
    await ws.prepare(request)
    await ws.send_str(
        json.dumps(
            {
                "event": "pusher:connection_established",
                "data": "{\"socket_id\":\"1.2\",\"activity_timeout\":30}",
            }
        )
    )
    async for msg in ws:
        if msg.type != WSMsgType.TEXT:
            continue
        packet = json.loads(msg.data)
        if packet.get("event") == "pusher:subscribe":
            await ws.send_str(json.dumps({"event": "chat.message", "data": {"id": 24}}))
            break
    return ws


async def mqtt_handler(request: web.Request) -> web.WebSocketResponse:
    ws = web.WebSocketResponse()
    await ws.prepare(request)
    await ws.send_str(json.dumps({"id": 25}))
    return ws


async def rsocket_handler(request: web.Request) -> web.WebSocketResponse:
    ws = web.WebSocketResponse()
    await ws.prepare(request)
    await ws.send_str(json.dumps({"id": 26}))
    return ws


async def sse_handler(request: web.Request) -> web.StreamResponse:
    response = web.StreamResponse(
        status=200,
        headers={
            "Content-Type": "text/event-stream",
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
        },
    )
    await response.prepare(request)
    await response.write(b"id: 1\n")
    await response.write(b"event: update\n")
    await response.write(b"data: {\"id\":7}\n\n")
    await response.write_eof()
    return response


async def poll_handler(request: web.Request) -> web.Response:
    return web.json_response({"id": 11})


app = web.Application()
app.add_routes([
    web.get("/ws", ws_handler),
    web.get("/socketio", socketio_handler),
    web.get("/stomp", stomp_handler),
    web.get("/graphql", graphql_handler),
    web.get("/reverb", reverb_handler),
    web.get("/mqtt", mqtt_handler),
    web.get("/rsocket", rsocket_handler),
    web.get("/sse", sse_handler),
    web.get("/poll", poll_handler),
])


if __name__ == "__main__":
    web.run_app(app, host="0.0.0.0", port=18080)
