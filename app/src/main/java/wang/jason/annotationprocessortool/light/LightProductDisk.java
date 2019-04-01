package wang.jason.annotationprocessortool.light;


import wang.jason.lib.FactoryAnnotation;

@FactoryAnnotation(route = "Disk",type = ILightProduct.class,params = {Integer.class})
public class LightProductDisk implements ILightProduct {


    public LightProductDisk(Integer integer) {

    }

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }
}
