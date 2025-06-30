package by.spvrent.utils;

import org.hashids.Hashids;

public class CryptoTool {
    private final Hashids hashids;

    public CryptoTool(String salt){
        int minHashLength = 10;
        this.hashids = new Hashids(salt,minHashLength);
    }
    public String hashOf(Long value){
        return hashids.encode(value);   // шифруем не кодируем
    }
    public Long idOf(String value){
        long[] res = hashids.decode(value); // расшифровываем
        if (res != null && res.length >0){
            return res[0];
        }
        return null;
    }
}
