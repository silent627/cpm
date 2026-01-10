import uvicorn
import os

if __name__ == "__main__":
    # 从环境变量获取配置，Docker 环境使用 0.0.0.0，本地开发使用 127.0.0.1
    host = os.getenv("HOST", "0.0.0.0")
    port = int(os.getenv("PORT", "8000"))
    reload = os.getenv("RELOAD", "false").lower() == "true"
    
    uvicorn.run("api.index:app", host=host, port=port, reload=reload)