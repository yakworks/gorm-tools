package yakworks.api;

import yakworks.i18n.MsgKey;

public interface ResultFluent<E extends ResultFluent> extends Result {
    default E title(String v) { setTitle(v);  return (E)this; }
    default E status(ApiStatus v) { setStatus(v); return (E)this; }
    default E status(Integer v) { setStatus(HttpStatus.valueOf(v)); return (E)this; }
    default E payload(Object v) { setPayload(v); return (E)this; }
    //aliases to payload
    default E value(Object v) { return payload(v); }

    default E msg(MsgKey v){ setMsg(v); return (E)this; }
    default E msg(String v) { return msg(MsgKey.ofCode(v));}
    default E msg(String v, Object args) { return msg(MsgKey.of(v, args));}
}
