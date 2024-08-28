import request from "../plugins/request";

export const getCurrentUser = async () => {
    const res:any = await request.get("/user/current")
    if (res.code === 0) {
        return res.data;
    }
    return null;
}
