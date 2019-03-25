package wang.jason.annotationprocessortool.light;


import wang.jason.lib.FactoryAnnotation;

@FactoryAnnotation(route = "Disk",type = ILightProduct.class)
public class LightProductDisk implements ILightProduct {


    public LightProductDisk() {

    }

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }
}
