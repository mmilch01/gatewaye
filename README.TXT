New in the release of August 2012:

 + Support for transfer of one series at a time both for C-MOVE and C-GET SCP (undertested). Performance on tested MR (one study, 23 series), CT (one study, 4 series) and MR perfusion derived maps (one study, 19 series) gains were, accordingly, 1.4x-2.5x, 3.16x-1.5x, 1.8x-1.6x.
 + All major imaging modalities are supported by C-GET
 + Simplified XNAT<->DICOM vocabulary format (flexible to some extent)
 + Changes in field mappings to match common DICOM->XNAT conversion
 + Interface support for “Allow anonymous AE C-GET retrieve”
 + Gateway version is displayed in the main window title
 - Non-DICOM UID emulation is no longer supported;
 + Fix in hard-coded REST variable name (scan ID) that resulted in erratic response in some cases
 + More DICOM fields (e.g. scan number) are returned
 + Search by study date, including ranges is now supported.