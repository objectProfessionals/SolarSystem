package com.op.solar;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

public class Base {

    String hostDir = "../host/solar/";

    public static void savePNGFile(BufferedImage opImage, String filePath, double dpi) {
        try {
            System.out.println("Saving...");
            File outfile = new File(filePath);
            // Find a jpeg writer
            ImageWriter writer = null;
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");
            IIOMetadata metadata = null;
            if (iter.hasNext()) {
                writer = iter.next();
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
                        .createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
                metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
                if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                    // continue;
                }
                double dpmm = dpi / 25.4;
                IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
                horiz.setAttribute("value", Double.toString(dpmm));
                IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
                vert.setAttribute("value", Double.toString(dpmm));
                IIOMetadataNode dim = new IIOMetadataNode("Dimension");
                dim.appendChild(horiz);
                dim.appendChild(vert);
                IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
                root.appendChild(dim);
                metadata.mergeTree("javax_imageio_1.0", root);
            }
            // Prepare output file
            ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
            writer.setOutput(ios);
            // Write the com.jhlabs.image
            writer.write(null, new IIOImage(opImage, null, metadata), null);
            // Cleanup
            ios.flush();
            writer.dispose();
            ios.close();
            opImage = null;
            System.gc();
            System.out.println("Saved " + filePath);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void saveJPGFile(BufferedImage opImage, String filePath, double dpi, float quality) {
        try {
            File outfile = new File(filePath);
            // Find a jpeg writer
            ImageWriter writer = null;
            Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpg");
            IIOMetadata metadata = null;
            if (iter.hasNext()) {
                writer = iter.next();
                ImageWriteParam writeParam = writer.getDefaultWriteParam();
                if (writeParam.canWriteCompressed()) {
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionQuality(0.05f);
                }
                ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier
                        .createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB);
                metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
                if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                    // continue;
                }
                double dpmm = dpi / 25.4;
                IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
                horiz.setAttribute("value", Double.toString(dpmm));
                IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
                vert.setAttribute("value", Double.toString(dpmm));
                IIOMetadataNode dim = new IIOMetadataNode("Dimension");
                dim.appendChild(horiz);
                dim.appendChild(vert);
                IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
                root.appendChild(dim);
                metadata.mergeTree("javax_imageio_1.0", root);
            }
            // Prepare output file
            ImageOutputStream ios = ImageIO.createImageOutputStream(outfile);
            writer.setOutput(ios);
            // Write the image
            writer.write(null, new IIOImage(opImage, null, metadata), null);
            // Cleanup
            ios.flush();
            writer.dispose();
            ios.close();
            opImage = null;
            System.gc();
            System.out.println("Saved " + filePath);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

//	public static void saveJPGFile(BufferedImage opImage, String filePath, double dpi, float quality) {
//		try {
//			File outfile = new File(filePath);
//			com.sun.image.codec.jpeg.JPEGEncodeParam param = com.sun.image.codec.jpeg.JPEGCodec
//					.getDefaultJPEGEncodeParam(opImage);
//			param.setXDensity((int) dpi);
//			param.setYDensity((int) dpi);
//			param.setQuality(quality, true);
//			param.setDensityUnit(com.sun.image.codec.jpeg.JPEGDecodeParam.DENSITY_UNIT_DOTS_INCH);
//			// Prepare output file
//			FileOutputStream fos = new FileOutputStream(outfile);
//			JPEGImageEncoder enc = JPEGCodec.createJPEGEncoder(fos, param);
//			enc.encode(opImage);
//			opImage = null;
//			System.gc();
//			System.out.println("saved: " + outfile.getAbsolutePath());
//		} catch (Exception e) {
//			System.err.println(e);
//		}
//	}
//
}
