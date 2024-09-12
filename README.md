## undertow_websockets

Using this to experiment with websockets, and their behavior when clients go offline. What I notice currently: 

**Chrome: When a client goes offline, Undertow does not close the connection.**

## To test

### Run the clojure server

```bash
cd clojure_server
clj -M:run
# endpoint now live on ws://localhost:8888/ws
```

### The the frontend

```bash
cd frontend
npm i
npm run dev
# frontend live on http://localhost:5173/
```
### Try to go offline
