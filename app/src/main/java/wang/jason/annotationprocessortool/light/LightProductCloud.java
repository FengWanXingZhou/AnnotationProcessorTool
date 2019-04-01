package wang.jason.annotationprocessortool.light;


import wang.jason.lib.FactoryAnnotation;

@FactoryAnnotation(route = "Cloud",type = ILightProduct.class,params = {LightProductDisk.class})
public class LightProductCloud implements ILightProduct {


    public LightProductCloud(LightProductDisk integer) {

    }

    @Override
    public void open() {

    }

    @Override
    public void close() {

    }
}
