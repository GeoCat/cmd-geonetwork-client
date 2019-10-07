package com.geocat.gnclient.gnservices.metadata.model;

public class MetadataResource {

    private String m_layerName;

    public MetadataResource()
    {
        super();
    }

    public MetadataResource(String inLayername)
    {
        this.m_layerName = inLayername;

    }

    public String getLayerName()
    {
        return m_layerName;
    }

    public void setLayerName(String m_layerName)
    {
        this.m_layerName = m_layerName;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("MetadataResource{");
        buf.append("layerName=");
        buf.append(getLayerName());
        buf.append("}");

        return buf.toString();
    }


}
