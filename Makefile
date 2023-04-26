JAVAC = javac
SRCDIR = src
BUILDDIR = build

SRCS := $(shell find $(SRCDIR) -name '*.java')
CLASSES := $(patsubst $(SRCDIR)/%.java,$(BUILDDIR)/%.class,$(SRCS))

.PHONY: all clean

all: $(BUILDDIR) $(CLASSES)

$(BUILDDIR):
	mkdir -p $(BUILDDIR)

$(BUILDDIR)/%.class: $(SRCS)
	$(JAVAC) -d $(BUILDDIR) $(SRCS)

clean:
	rm -rf $(BUILDDIR)

$(CLASSES): $(SRCS)
